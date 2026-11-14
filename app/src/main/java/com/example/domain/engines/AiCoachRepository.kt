package com.example.domain.engines

import com.example.data.db.ClientEntity

data class AiAssistantReply(
    val title: String,
    val message: String,
    val actionLabel: String? = null,
    val actionType: AiActionType? = null,
    val isPreviewPlaceholder: Boolean = true
)

enum class AiActionType {
    GENERATE_FULL_BODY_SESSION,
    APPLY_HIGH_PROTEIN_MACROS,
    SWITCH_SPLIT_UPPER_LOWER,
    CREATE_HIIT_CIRCUIT
}

interface AiCoachRepository {
    suspend fun processNaturalCommand(prompt: String, activeClient: ClientEntity?): AiAssistantReply
}

/**
 * Implémentation locale de démonstration lorsque Firebase (AI Logic) n'est pas activé.
 * Exécute des actions réelles sur les moteurs de la plateforme tout en affichant
 * clairement la notice d'aperçu IA.
 */
class PlaceholderAiCoachRepository : AiCoachRepository {
    override suspend fun processNaturalCommand(
        prompt: String,
        activeClient: ClientEntity?
    ): AiAssistantReply {
        val q = prompt.lowercase()
        val clientName = activeClient?.fullName ?: "le pratiquant actif"

        return when {
            q.contains("macro") || q.contains("nutrition") || q.contains("prot") || q.contains("calorie") -> {
                if (activeClient?.hasRestrictiveDietBan == true && (q.contains("keto") || q.contains("low carb") || q.contains("jeûne") || q.contains("psmf"))) {
                    AiAssistantReply(
                        title = "Sécurité Nutritionnelle Déclenchée",
                        message = "Action bloquée pour $clientName : les régimes restrictifs (Keto, Low Carb, Jeûne, PSMF) sont strictement interdits pour les mineurs (< 18 ans) et cas médicaux. Stratégie Équilibrée maintenue.",
                        actionLabel = null,
                        actionType = null
                    )
                } else {
                    AiAssistantReply(
                        title = "Recalcul Nutritionnel Proposé",
                        message = "Je recommande de basculer $clientName vers une répartition High Protein (35% Protéines / 38% Glucides / 27% Lipides) avec intégration des compléments actifs dans les totaux.",
                        actionLabel = "Appliquer High Protein",
                        actionType = AiActionType.APPLY_HIGH_PROTEIN_MACROS
                    )
                }
            }
            q.contains("hiit") || q.contains("circuit") || q.contains("cardio") -> {
                AiAssistantReply(
                    title = "Circuit HIIT Métabolique Prêt",
                    message = "Génération d'un circuit HIIT 40s effort / 20s récupération (3 tours, 10 stations polyarticulaires) compatible avec le matériel de $clientName.",
                    actionLabel = "Générer le Circuit HIIT",
                    actionType = AiActionType.CREATE_HIIT_CIRCUIT
                )
            }
            q.contains("split") || q.contains("programme") || q.contains("haut") || q.contains("muscu") -> {
                AiAssistantReply(
                    title = "Optimisation du Split Musculation",
                    message = "Passage recommandé vers un split Haut / Bas sur 4 séances hebdomadaires pour $clientName afin d'optimiser la fréquence de stimulation (2x/semaine par groupe musculaire).",
                    actionLabel = "Générer Split Haut / Bas",
                    actionType = AiActionType.SWITCH_SPLIT_UPPER_LOWER
                )
            }
            else -> {
                AiAssistantReply(
                    title = "Séance Complète 6 Phases Prête",
                    message = "Préparation d'une séance Full Body complète respectant les 6 phases réglementaires (échauffement général, articulaire sans forcer le cou, activation, 13 exercices principaux, retour au calme et étirements).",
                    actionLabel = "Générer la Séance 6 Phases",
                    actionType = AiActionType.GENERATE_FULL_BODY_SESSION
                )
            }
        }
    }
}
