package com.example.data.seed

import com.example.data.db.FoodEntity
import com.example.data.db.SupplementEntity

object FoodAndSupplementCatalog {
    val foods: List<FoodEntity> = listOf(
        // Protéines
        FoodEntity(0, "Blanc de poulet (cru)", "Protéines", 110.0, 23.0, 0.0, 1.5, 0.0, "", "Cru", "Dinde escalope (cru)"),
        FoodEntity(0, "Blanc de poulet (cuit)", "Protéines", 155.0, 31.0, 0.0, 2.5, 0.0, "", "Cuit", "Tofu ferme nature"),
        FoodEntity(0, "Steak haché 5% MG (cru)", "Protéines", 132.0, 21.0, 0.0, 5.0, 0.0, "", "Cru", "Tempeh bio"),
        FoodEntity(0, "Filet de saumon (cru)", "Protéines", 206.0, 20.0, 0.0, 13.5, 0.0, "poissons", "Cru", "Filet de poulet + huile de noix"),
        FoodEntity(0, "Cabillaud dos (cuit)", "Protéines", 92.0, 21.0, 0.0, 0.8, 0.0, "poissons", "Cuit", "Blanc de dinde (cuit)"),
        FoodEntity(0, "Œufs entiers (calibre M)", "Protéines", 145.0, 12.6, 0.7, 10.2, 0.0, "oeufs", "Cuit", "Tofu soyeux / Protéine pois"),
        FoodEntity(0, "Blancs d'œufs liquides", "Protéines", 48.0, 10.8, 0.7, 0.1, 0.0, "oeufs", "Cuit", "Seitan ou Tofu ferme"),
        FoodEntity(0, "Tofu ferme nature", "Protéines", 138.0, 14.5, 1.9, 8.0, 1.2, "soja", "Cuit", "Blanc de poulet (cuit)"),
        FoodEntity(0, "Lentilles vertes (cuites)", "Protéines", 116.0, 9.0, 20.0, 0.4, 7.9, "", "Cuit", "Pois chiches (cuits)"),
        FoodEntity(0, "Pois chiches (cuits)", "Protéines", 164.0, 8.9, 27.4, 2.6, 7.6, "", "Cuit", "Haricots rouges (cuits)"),
        FoodEntity(0, "Crevettes décortiquées (cuites)", "Protéines", 99.0, 24.0, 0.2, 0.3, 0.0, "poissons", "Cuit", "Blanc de poulet (cuit)"),
        FoodEntity(0, "Dinde escalope (cru)", "Protéines", 107.0, 23.5, 0.0, 1.1, 0.0, "", "Cru", "Tofu ferme nature"),

        // Produits Laitiers & Alternatives sans lactose
        FoodEntity(0, "Fromage blanc 0% MG", "Produits Laitiers & Alt.", 48.0, 8.0, 3.8, 0.1, 0.0, "lactose", "Cru", "Yaourt soja nature enrichi"),
        FoodEntity(0, "Skyr nature 0%", "Produits Laitiers & Alt.", 63.0, 11.0, 4.0, 0.2, 0.0, "lactose", "Cru", "Yaourt soja nature enrichi"),
        FoodEntity(0, "Yaourt grec 2% MG", "Produits Laitiers & Alt.", 73.0, 9.5, 3.6, 2.0, 0.0, "lactose", "Cru", "Yaourt coco-soja sans sucre"),
        FoodEntity(0, "Yaourt soja nature enrichi", "Produits Laitiers & Alt.", 46.0, 4.8, 1.4, 2.3, 0.6, "soja", "Cru", "Yaourt avoine-amande sans sucre"),
        FoodEntity(0, "Boisson amande sans sucre", "Produits Laitiers & Alt.", 15.0, 0.5, 0.2, 1.2, 0.4, "fruits_a_coque", "Cru", "Boisson riz enrichie"),

        // Glucides
        FoodEntity(0, "Flocons d'avoine (crus)", "Glucides", 374.0, 13.5, 58.7, 7.0, 10.1, "gluten", "Cru", "Flocons de sarrasin (crus)"),
        FoodEntity(0, "Flocons de sarrasin (crus)", "Glucides", 346.0, 12.6, 70.5, 3.1, 9.5, "", "Cru", "Riz basmati (cuit)"),
        FoodEntity(0, "Riz basmati (cru)", "Glucides", 352.0, 7.8, 77.5, 0.8, 1.4, "", "Cru", "Quinoa (cru)"),
        FoodEntity(0, "Riz basmati (cuit)", "Glucides", 123.0, 2.7, 27.2, 0.3, 0.5, "", "Cuit", "Quinoa (cuit)"),
        FoodEntity(0, "Quinoa (cuit)", "Glucides", 120.0, 4.4, 21.3, 1.9, 2.8, "", "Cuit", "Riz basmati (cuit)"),
        FoodEntity(0, "Patate douce (cuite)", "Glucides", 86.0, 1.6, 20.1, 0.1, 3.0, "", "Cuit", "Pomme de terre vapeur"),
        FoodEntity(0, "Pomme de terre vapeur", "Glucides", 77.0, 2.0, 17.5, 0.1, 2.2, "", "Cuit", "Patate douce (cuite)"),
        FoodEntity(0, "Pâtes complètes (cuites)", "Glucides", 131.0, 5.3, 26.5, 0.9, 4.2, "gluten", "Cuit", "Pâtes de lentilles corail (cuites)"),
        FoodEntity(0, "Pâtes de lentilles corail (cuites)", "Glucides", 142.0, 10.2, 22.8, 0.8, 4.9, "", "Cuit", "Riz basmati (cuit)"),
        FoodEntity(0, "Pain complet au levain", "Glucides", 242.0, 8.8, 44.0, 1.8, 6.8, "gluten", "Cru", "Pain sans gluten sarrasin"),
        FoodEntity(0, "Pain sans gluten sarrasin", "Glucides", 230.0, 6.2, 42.0, 3.2, 5.4, "", "Cru", "Galettes de riz complet"),

        // Lipides
        FoodEntity(0, "Huile d'olive vierge extra", "Lipides", 884.0, 0.0, 0.0, 100.0, 0.0, "", "Cru", "Huile de colza"),
        FoodEntity(0, "Avocat frais", "Lipides", 160.0, 2.0, 8.5, 14.7, 6.7, "", "Cru", "Huile d'olive vierge extra"),
        FoodEntity(0, "Amandes entières", "Lipides", 579.0, 21.2, 21.6, 49.9, 12.5, "fruits_a_coque", "Cru", "Graines de courge"),
        FoodEntity(0, "Beurre de cacahuète 100%", "Lipides", 598.0, 25.0, 16.0, 50.0, 8.0, "arachides", "Cru", "Purée de graines de tournesol"),
        FoodEntity(0, "Graines de chia", "Lipides", 486.0, 16.5, 42.1, 30.7, 34.4, "", "Cru", "Graines de lin moulues"),
        FoodEntity(0, "Graines de courge", "Lipides", 559.0, 30.2, 10.7, 49.0, 6.0, "", "Cru", "Huile de colza"),
        FoodEntity(0, "Noix de Grenoble", "Lipides", 654.0, 15.2, 13.7, 65.2, 6.7, "fruits_a_coque", "Cru", "Graines de lin moulues"),

        // Légumes & Fibres
        FoodEntity(0, "Brocolis vapeur", "Légumes & Fibres", 35.0, 2.4, 7.2, 0.4, 3.3, "", "Cuit", "Haricots verts vapeur"),
        FoodEntity(0, "Haricots verts vapeur", "Légumes & Fibres", 31.0, 1.8, 7.0, 0.1, 3.4, "", "Cuit", "Courgettes grillées"),
        FoodEntity(0, "Épinards frais", "Légumes & Fibres", 23.0, 2.9, 3.6, 0.4, 2.2, "", "Cru", "Mâche fraîche"),
        FoodEntity(0, "Courgettes grillées", "Légumes & Fibres", 21.0, 1.2, 3.5, 0.3, 1.1, "", "Cuit", "Poivrons grillés"),
        FoodEntity(0, "Tomates cerises", "Légumes & Fibres", 18.0, 0.9, 3.9, 0.2, 1.2, "", "Cru", "Concombre frais"),

        // Fruits
        FoodEntity(0, "Banane mûre", "Fruits", 89.0, 1.1, 22.8, 0.3, 2.6, "", "Cru", "Compote pomme sans sucre"),
        FoodEntity(0, "Myrtilles fraîches", "Fruits", 57.0, 0.7, 14.5, 0.3, 2.4, "", "Cru", "Framboises fraîches"),
        FoodEntity(0, "Pomme Golden", "Fruits", 52.0, 0.3, 13.8, 0.2, 2.4, "", "Cru", "Poire fraîche"),
        FoodEntity(0, "Kiwi vert", "Fruits", 61.0, 1.1, 14.7, 0.5, 3.0, "", "Cru", "Orange fraîche")
    )

    fun defaultSupplementsForClient(clientId: Long, hasLactoseAllergy: Boolean, isMinor: Boolean): List<SupplementEntity> {
        val list = mutableListOf<SupplementEntity>()
        if (!hasLactoseAllergy) {
            list += SupplementEntity(
                clientId = clientId,
                name = "Whey Isolate Native (30g poudre)",
                category = "Protéine",
                powderDoseG = 30.0,
                activeProteinG = 26.4,
                activeCarbsG = 0.8,
                activeFatG = 0.4,
                activeKcal = 112,
                creatineG = 0.0,
                caffeineMg = 0,
                timing = "Post-entraînement",
                includedInTotals = true
            )
        } else {
            list += SupplementEntity(
                clientId = clientId,
                name = "Isolat Protéine de Pois & Riz (32g poudre)",
                category = "Protéine",
                powderDoseG = 32.0,
                activeProteinG = 25.0,
                activeCarbsG = 1.6,
                activeFatG = 1.1,
                activeKcal = 116,
                creatineG = 0.0,
                caffeineMg = 0,
                timing = "Post-entraînement",
                includedInTotals = true
            )
        }
        if (!isMinor) {
            list += SupplementEntity(
                clientId = clientId,
                name = "Créatine Monohydrate Creapure (5g poudre)",
                category = "Performance",
                powderDoseG = 5.0,
                activeProteinG = 0.0,
                activeCarbsG = 0.0,
                activeFatG = 0.0,
                activeKcal = 0,
                creatineG = 5.0,
                caffeineMg = 0,
                timing = "Post-entraînement",
                includedInTotals = true
            )
        }
        list += SupplementEntity(
            clientId = clientId,
            name = "Oméga-3 EPA/DHA Purifiés (3 capsules)",
            category = "Santé & Récupération",
            powderDoseG = 3.0,
            activeProteinG = 0.0,
            activeCarbsG = 0.0,
            activeFatG = 3.0,
            activeKcal = 27,
            creatineG = 0.0,
            caffeineMg = 0,
            timing = "Dîner",
            includedInTotals = true
        )
        return list
    }

    val supplementTemplates: List<SupplementEntity> = listOf(
        SupplementEntity(0, 0, "Whey Isolate Native (30g poudre)", "Protéine", 30.0, 26.4, 0.8, 0.4, 112, 0.0, 0, "Post-entraînement", true),
        SupplementEntity(0, 0, "Caséine Micellaire (30g poudre)", "Protéine", 30.0, 24.5, 1.2, 0.6, 108, 0.0, 0, "Coucher", true),
        SupplementEntity(0, 0, "Isolat Végétal Pois/Riz (32g poudre)", "Protéine", 32.0, 25.0, 1.6, 1.1, 116, 0.0, 0, "Post-entraînement", true),
        SupplementEntity(0, 0, "Créatine Monohydrate (5g poudre)", "Performance", 5.0, 0.0, 0.0, 0.0, 0, 5.0, 0, "Post-entraînement", true),
        SupplementEntity(0, 0, "Maltodextrine / Cluster Dextrin (35g poudre)", "Performance", 35.0, 0.0, 33.2, 0.0, 133, 0.0, 0, "Intra-entraînement", true),
        SupplementEntity(0, 0, "Oméga-3 EPA/DHA (3g capsules)", "Santé & Récupération", 3.0, 0.0, 0.0, 3.0, 27, 0.0, 0, "Dîner", true),
        SupplementEntity(0, 0, "Magnésium Bisglycinate (2,5g poudre)", "Santé & Récupération", 2.5, 0.0, 0.0, 0.0, 0, 0.0, 0, "Coucher", true),
        SupplementEntity(0, 0, "Pré-Workout Caféiné (12g poudre)", "Performance", 12.0, 0.0, 1.5, 0.0, 6, 0.0, 200, "Pré-entraînement", true)
    )
}
