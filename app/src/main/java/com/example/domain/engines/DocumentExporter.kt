package com.example.domain.engines

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.db.AssessmentEntity
import com.example.data.db.ClientEntity
import com.example.data.db.MealItemEntity
import com.example.data.db.NutritionProgramEntity
import com.example.data.db.TrainingDayExerciseEntity
import com.example.data.db.WorkoutLogEntity
import com.example.data.db.WorkoutSessionEntity
import com.example.data.db.WorkoutSessionRowEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
    PDF("pdf", "application/pdf", "PDF (.pdf)"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word (.docx)"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel (.xlsx)")
}

enum class ExportScope(val label: String) {
    SESSION_6_PHASES("Séance Complète 6 Phases"),
    GYM_PROGRAM("Programme de Musculation"),
    NUTRITION_PLAN("Plan Nutrition & Compléments"),
    PROGRESS_REPORT("Bilan Suivi & Progrès")
}

object DocumentExporter {

    fun exportAndShare(
        context: Context,
        format: ExportFormat,
        scope: ExportScope,
        client: ClientEntity?,
        session: WorkoutSessionEntity?,
        sessionRows: List<WorkoutSessionRowEntity>,
        programTitle: String,
        programRows: List<TrainingDayExerciseEntity>,
        nutrition: NutritionProgramEntity?,
        mealItems: List<MealItemEntity>,
        assessments: List<AssessmentEntity>,
        workoutLogs: List<WorkoutLogEntity>
    ): Result<File> {
        return runCatching {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.FRANCE).format(Date())
            val safeClient = (client?.fullName ?: "Sportif").replace(Regex("[^A-Za-z0-9_-]"), "_")
            val fileName = "SportCoachPro_${scope.name}_${safeClient}_$stamp.${format.extension}"
            val outFile = File(exportDir, fileName)

            val (title, headers, tableRows) = buildDocumentContent(
                scope, client, session, sessionRows, programTitle, programRows,
                nutrition, mealItems, assessments, workoutLogs
            )

            when (format) {
                ExportFormat.PDF -> writePdf(outFile, title, headers, tableRows)
                ExportFormat.DOCX -> writeDocx(outFile, title, headers, tableRows)
                ExportFormat.XLSX -> writeXlsx(outFile, title, headers, tableRows)
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Exporter : $title")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            outFile
        }
    }

    private fun buildDocumentContent(
        scope: ExportScope,
        client: ClientEntity?,
        session: WorkoutSessionEntity?,
        sessionRows: List<WorkoutSessionRowEntity>,
        programTitle: String,
        programRows: List<TrainingDayExerciseEntity>,
        nutrition: NutritionProgramEntity?,
        mealItems: List<MealItemEntity>,
        assessments: List<AssessmentEntity>,
        workoutLogs: List<WorkoutLogEntity>
    ): Triple<String, List<String>, List<List<String>>> {
        val clientLabel = client?.let { "${it.fullName} (${it.age} ans, ${it.goal})" } ?: "Client"
        return when (scope) {
            ExportScope.SESSION_6_PHASES -> Triple(
                "Séance : ${session?.title ?: "Complète"} — $clientLabel",
                listOf("N°", "Phase", "Exercice", "Région", "Séries", "Reps", "Durée", "Récup", "Exécution"),
                sessionRows.map {
                    listOf(
                        it.rowNumber.toString(),
                        it.phase,
                        it.exerciseName,
                        it.region,
                        it.sets,
                        it.reps,
                        it.durationText,
                        it.restText,
                        it.executionCues
                    )
                }
            )
            ExportScope.GYM_PROGRAM -> Triple(
                "Programme : $programTitle — $clientLabel",
                listOf("Jour", "Ordre", "Exercice", "Muscles", "Séries", "Reps", "Repos", "Tempo", "Charge"),
                programRows.map {
                    listOf(
                        it.dayLabel,
                        it.orderIndex.toString(),
                        it.exerciseName,
                        it.muscleGroups,
                        it.sets.toString(),
                        it.reps,
                        "${it.restSeconds}s",
                        it.tempo,
                        if (it.targetLoadKg > 0) "${it.targetLoadKg} kg" else "PDC"
                    )
                }
            )
            ExportScope.NUTRITION_PLAN -> Triple(
                "Nutrition (${nutrition?.strategy ?: "Équilibrée"}) — $clientLabel | ${nutrition?.targetKcal ?: 0} kcal",
                listOf("Aliment", "Grammage", "État", "Kcal", "Prot (g)", "Gluc (g)", "Lip (g)", "Fibres (g)", "Substitution"),
                mealItems.map {
                    listOf(
                        it.foodName,
                        "${it.grams} g",
                        it.prepState,
                        "${it.kcal}",
                        "${it.proteinG}",
                        "${it.carbsG}",
                        "${it.fatG}",
                        "${it.fiberG}",
                        if (it.wasAutoSubstituted) "Substitué (${it.originalFoodName})" else "—"
                    )
                }
            )
            ExportScope.PROGRESS_REPORT -> {
                val df = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                val rows = assessments.map {
                    listOf(
                        df.format(Date(it.dateEpochMs)),
                        it.entryType,
                        it.weightKg?.let { w -> "$w kg" } ?: "—",
                        it.bodyFatPct?.let { bf -> "$bf %" } ?: "—",
                        it.waistCm?.let { wc -> "$wc cm" } ?: "—",
                        it.caloriesConsumed?.let { c -> "$c kcal" } ?: "—",
                        it.notes
                    )
                } + workoutLogs.map {
                    listOf(
                        df.format(Date(it.dateEpochMs)),
                        "Séance : ${it.exerciseName}",
                        "${it.loadKg} kg",
                        "${it.setsCompleted}x${it.repsCompleted}",
                        "RPE ${it.rpe}",
                        "${it.totalVolumeKg.toInt()} kg vol.",
                        if (it.isPr) "PR RECORD" else it.notes
                    )
                }
                Triple(
                    "Bilan Suivi & Progrès — $clientLabel",
                    listOf("Date", "Type / Exercice", "Poids / Charge", "MG% / Reps", "Taille / RPE", "Kcal / Volume", "Notes"),
                    rows
                )
            }
        }
    }

    private fun writePdf(file: File, title: String, headers: List<String>, rows: List<List<String>>) {
        val pdf = PdfDocument()
        val pageWidth = 842
        val pageHeight = 595
        val titlePaint = Paint().apply { color = Color.BLACK; textSize = 15f; isFakeBoldText = true }
        val headerPaint = Paint().apply { color = Color.rgb(30, 41, 59); textSize = 9.5f; isFakeBoldText = true }
        val cellPaint = Paint().apply { color = Color.DKGRAY; textSize = 8.5f }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        var pageNum = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
        var canvas = page.canvas
        var y = 40f

        canvas.drawText("SPORT COACH PRO — $title", 28f, y, titlePaint)
        y += 24f

        val colWidth = (pageWidth - 56f) / headers.size.coerceAtLeast(1)
        headers.forEachIndexed { idx, h ->
            canvas.drawText(h.take(16), 28f + idx * colWidth, y, headerPaint)
        }
        y += 8f
        canvas.drawLine(28f, y, pageWidth - 28f, y, linePaint)
        y += 14f

        rows.forEach { row ->
            if (y > pageHeight - 36f) {
                pdf.finishPage(page)
                pageNum++
                page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
                canvas = page.canvas
                y = 40f
            }
            row.forEachIndexed { idx, cell ->
                canvas.drawText(cell.take(22), 28f + idx * colWidth, y, cellPaint)
            }
            y += 16f
        }
        pdf.finishPage(page)
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
    }

    private fun xmlEscape(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun writeDocx(file: File, title: String, headers: List<String>, rows: List<List<String>>) {
        val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

        val rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>""")
        sb.append("""<w:p><w:r><w:rPr><w:b/><w:sz w:val="32"/></w:rPr><w:t>${xmlEscape("SPORT COACH PRO — $title")}</w:t></w:r></w:p>""")
        sb.append("""<w:tbl><w:tblPr><w:tblBorders><w:top w:val="single" w:sz="4"/><w:bottom w:val="single" w:sz="4"/><w:insideH w:val="single" w:sz="4"/><w:insideV w:val="single" w:sz="4"/></w:tblBorders></w:tblPr>""")
        fun appendRow(cells: List<String>, bold: Boolean) {
            sb.append("<w:tr>")
            cells.forEach { c ->
                sb.append("<w:tc><w:p><w:r>")
                if (bold) sb.append("<w:rPr><w:b/></w:rPr>")
                sb.append("<w:t>${xmlEscape(c)}</w:t></w:r></w:p></w:tc>")
            }
            sb.append("</w:tr>")
        }
        appendRow(headers, true)
        rows.forEach { appendRow(it, false) }
        sb.append("</w:tbl><w:sectPr/></w:body></w:document>")

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(contentTypes.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(rels.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("word/document.xml"))
            zip.write(sb.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }

    private fun writeXlsx(file: File, title: String, headers: List<String>, rows: List<List<String>>) {
        val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

        val rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets><sheet name="SportCoachPro" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

        val workbookRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""

        val sheet = StringBuilder()
        sheet.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sheet.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
        val allRows = listOf(listOf(title), headers) + rows
        allRows.forEachIndexed { rIdx, r ->
            val rowNum = rIdx + 1
            sheet.append("""<row r="$rowNum">""")
            r.forEachIndexed { cIdx, v ->
                val colLetter = ('A' + cIdx).toString()
                sheet.append("""<c r="$colLetter$rowNum" t="inlineStr"><is><t>${xmlEscape(v)}</t></is></c>""")
            }
            sheet.append("</row>")
        }
        sheet.append("</sheetData></worksheet>")

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(contentTypes.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(rels.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/workbook.xml"))
            zip.write(workbook.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
            zip.write(workbookRels.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zip.write(sheet.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }
}
