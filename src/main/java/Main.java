import java.util.List;

public class Main {

    public static void main(String[] args) {

        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("------------- Test A -------------");
        Dish dishA = dataRetriever.findDishById(4);
        if (dishA != null) {
            dishA.prettyPrint();
        } else {
            System.out.println("⚠️ Plat non trouvé !");
        }

        System.out.println("------------- Test B -------------");
        Dish dishB = dataRetriever.findDishById(2);
        if (dishB != null) {
            dishB.prettyPrint();
        } else {
            System.out.println("⚠️ Plat non trouvé !");
        }


        System.out.println("------------- Test C -------------");
        List<Ingredient> ingredientsC = dataRetriever.findIngredients(1, 2);
        printIngredients(ingredientsC);


        System.out.println("------------- Test D -------------");
        List<Ingredient> ingredientsD = dataRetriever.findIngredients(3, 5);
        printIngredients(ingredientsD);


        System.out.println("------------- Test E -------------");
        List<Dish> dishesE = dataRetriever.findDishsByIngredientName("laitue");
        printDishes(dishesE);


        System.out.println("------------- Test F -------------");
        List<Ingredient> ingredientsF = dataRetriever.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10);
        printIngredients(ingredientsF);


        System.out.println("------------- Test G -------------");
        List<Ingredient> ingredientsG = dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10);
        printIngredients(ingredientsG);

        System.out.println("------------- Test H -------------");
        List<Ingredient> ingredientsH = dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10);
        printIngredients(ingredientsH);
    }


    public static void printIngredients(List<Ingredient> ingredients) {
        System.out.println("🥬 Ingrédients");
        System.out.println("---------------------------------");

        if (ingredients == null || ingredients.isEmpty()) {
            System.out.println("⚠️ Aucun ingrédient trouvé !");
        } else {

            for (Ingredient ing : ingredients) {
                System.out.println( "️⃣ " + ing.getName());
                ing.prettyPrint();
                System.out.println();
            }
        }

        System.out.println("---------------------------------");
    }


    public static void printDishes(List<Dish> dishes) {
        System.out.println("🍽️ Plats");
        System.out.println("---------------------------------");

        if (dishes == null || dishes.isEmpty()) {
            System.out.println("⚠️ Aucun plat trouvé !");
        } else {

            for (Dish dish : dishes) {
                System.out.println("️⃣ " + dish.getName());
                dish.prettyPrint();
                System.out.println();
            }
        }

        System.out.println("---------------------------------");
    }
}
