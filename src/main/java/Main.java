import javax.crypto.spec.PSource;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        DataRetriever dataRetriever = new DataRetriever();
//        System.out.println("---Test A ---");
//        Dish dish = dataRetriever.findDishById(4);
//        System.out.println(dish);
//        System.out.println("---Test B ---");
//        Dish dish1 = dataRetriever.findDishById(2);
//        System.out.println(dish1);
//
//        System.out.println("---Test C ---");
//        System.out.println(dataRetriever.findIngredients(1, 2));
//
//        System.out.println("---Test D ---");
//        System.out.println(dataRetriever.findIngredients(3, 5));
//
//        System.out.println("---Test E ---");
//        System.out.println(dataRetriever.findDishsByIngredientName("laitue"));
//
//        System.out.println("---Test F ---");
//        System.out.println(dataRetriever.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10));
//
//        System.out.println("---Test G ---");
//        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10));
//        System.out.println("---Test H ---");
//        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10));
//
//        System.out.println("---Test I ---");
//        List<Ingredient> newIngredients1 = List.of(
//                new Ingredient( 21,"Farine", 1200.0, CategoryEnum.OTHER),
//                new Ingredient( "Levure", 500.0, CategoryEnum.OTHER)
//        );
//
//        try {
//            List<Ingredient> created1 = dataRetriever.createIngredients(newIngredients1);
//            System.out.println("Test 1 réussi : ingrédients créés :");
//            for (Ingredient ing : created1) {
//                System.out.println("- " + ing.getName() + " (ID=" + ing.getId() + ")");
//            }
//        } catch (RuntimeException e) {
//            System.out.println("Test 1 échoué : " + e.getMessage());
//        }
//
//
//        System.out.println("---Test J ---");
//        List<Ingredient> newIngredients2 = List.of(
//                new Ingredient( "Carotte", 2000.0, CategoryEnum.VEGETABLE),
//                new Ingredient( "Laitue", 2000.0, CategoryEnum.VEGETABLE)
//        );
//
//        try {
//            List<Ingredient> created1 = dataRetriever.createIngredients(newIngredients2);
//            System.out.println("Test 1 réussi : ingrédients créés :");
//            for (Ingredient ing : created1) {
//                System.out.println("- " + ing.getName() + " (ID=" + ing.getId() + ")");
//            }
//        } catch (RuntimeException e) {
//            System.out.println("Test 1 échoué : " + e.getMessage());
//        }
//

        try {
            // 1️⃣ Créer quelques ingrédients
            Ingredient i1 = new Ingredient(null, "Poulet", 4500.0, CategoryEnum.ANIMAL);
            Ingredient i2 = new Ingredient(null, "Sel", 200.0, CategoryEnum.OTHER);
            Ingredient i3 = new Ingredient(null, "Poivre", 300.0, CategoryEnum.OTHER);

            List<Ingredient> savedIngredients = dataRetriever.createIngredients(List.of(i1, i2, i3));
            System.out.println("--- Ingrédients créés ---");
            savedIngredients.forEach(System.out::println);

            // 2️⃣ Créer un plat avec ces ingrédients
            DishIngredient di1 = new DishIngredient();
            di1.setIngredient(savedIngredients.get(0));
            di1.setQuantity(1.0);
            di1.setUnit(Unit.KG);

            DishIngredient di2 = new DishIngredient();
            di2.setIngredient(savedIngredients.get(1));
            di2.setQuantity(0.01);
            di2.setUnit(Unit.KG);

            DishIngredient di3 = new DishIngredient();
            di3.setIngredient(savedIngredients.get(2));
            di3.setQuantity(0.005);
            di3.setUnit(Unit.KG);

            Dish dish = new Dish();
            dish.setName("Poulet grillé");
            dish.setDishType(DishTypeEnum.MAIN);
            dish.setPrice(12000.0);
            dish.setIngredients(List.of(di1, di2, di3));

            Dish savedDish = dataRetriever.saveDish(dish);
            System.out.println("\n--- Plat créé ---");
            System.out.println(savedDish);

            // 3️⃣ Mettre à jour le plat : changer quantité et retirer un ingrédient
            di1.setQuantity(1.2); // augmenter le poulet
            Dish updatedDish = new Dish();
            updatedDish.setId(savedDish.getId()); // garder le même id
            updatedDish.setName("Poulet grillé épicé");
            updatedDish.setDishType(DishTypeEnum.MAIN);
            updatedDish.setPrice(13000.0);
            updatedDish.setIngredients(List.of(di1, di3)); // retirer le sel

            Dish savedUpdatedDish = dataRetriever.saveDish(updatedDish);
            System.out.println("\n--- Plat mis à jour ---");
            System.out.println(savedUpdatedDish);

        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println("---Prix ---");
//        System.out.println("Nom du plat : " + salade.getName());
//        System.out.println("Coût du plat : " + salade.getDishCost() + " Ar");
//        System.out.println("Marge brute  : " + salade.getGrossMargin() + " Ar");


    }
}
