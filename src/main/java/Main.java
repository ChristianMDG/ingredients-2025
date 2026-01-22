import javax.crypto.spec.PSource;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        DataRetriever dataRetriever = new DataRetriever();
        System.out.println("---Test A ---");
        Dish dish = dataRetriever.findDishById(4);
        System.out.println(dish);
        System.out.println("---Test B ---");
        Dish dish1 = dataRetriever.findDishById(2);
        System.out.println(dish1);

//        System.out.println("---Test C ---");
//        System.out.println(dataRetriever.findIngredients(1, 2));
//
//        System.out.println("---Test D ---");
//        System.out.println(dataRetriever.findIngredients(3, 5));
//
//        System.out.println("---Test E ---");
//        System.out.println(dataRetriever.findDishsByIngredientName("lai"));
//
//        System.out.println("---Test F ---");
//        System.out.println(dataRetriever.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10));
//
//        System.out.println("---Test G ---");
//        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10));
//        System.out.println("---Test H ---");
//        System.out.println(dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10));

//        System.out.println("---Test I ---");
//        List<Ingredient> newIngredients1 = List.of(
//                new Ingredient( "Fromage", 1200.0, CategoryEnum.ANIMAL),
//                new Ingredient( "Oignon", 500.0, CategoryEnum.VEGETABLE)
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
//            List<Ingredient> created1 = dataRetriever.createIngredients(newIngredients1);
//            System.out.println("Test 1 réussi : ingrédients créés :");
//            for (Ingredient ing : created1) {
//                System.out.println("- " + ing.getName() + " (ID=" + ing.getId() + ")");
//            }
//        } catch (RuntimeException e) {
//            System.out.println("Test 1 échoué : " + e.getMessage());
//        }
//    }

//        Ingredient laitue = new Ingredient(1, "Laitue", 1000.0, CategoryEnum.VEGETABLE);
//        Ingredient tomate = new Ingredient(2, "Tomate", 600.0, CategoryEnum.VEGETABLE);
//
//
//
//        DishIngredient di1 = new DishIngredient(laitue,1.0,Unit.KG);
//        DishIngredient di2 = new DishIngredient(tomate,1.0,Unit.KG);
//
//        List<DishIngredient> ingredients = List.of(di1, di2);
//        Dish salade = new Dish(
//                "Salade fraîche", DishTypeEnum.STARTER, 2000.0,ingredients
//
//        );
//        System.out.println("---Prrix ---");
//        System.out.println("Nom du plat : " + salade.getName());
//        System.out.println("Coût du plat : " + salade.getDishCost() + " Ar");
//        System.out.println("Marge brute  : " + salade.getGrossMargin() + " Ar");
//
//
    }
}
