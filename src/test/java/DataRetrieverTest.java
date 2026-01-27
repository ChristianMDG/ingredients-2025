import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataRetrieverTest {

    private DataRetriever dataRetriever;

    @BeforeEach
    void setUp() {
        dataRetriever = new DataRetriever();
    }

    @Test
    void testA_findDishById_1() {
        System.out.println("------------- Test A -------------");
        Dish dishA = dataRetriever.findDishById(1);

        assertNotNull(dishA, "⚠️ Plat non trouvé !");
        dishA.prettyPrint();
    }

    @Test
    void testB_findDishById_2() {
        System.out.println("------------- Test B -------------");
        Dish dishB = dataRetriever.findDishById(2);

        assertNotNull(dishB, "⚠️ Plat non trouvé !");
        dishB.prettyPrint();
    }

    @Test
    void testC_findIngredients_1_2() {
        System.out.println("------------- Test C -------------");
        List<Ingredient> ingredientsC = dataRetriever.findIngredients(1, 2);

        assertNotNull(ingredientsC);
        assertFalse(ingredientsC.isEmpty());

        printIngredients(ingredientsC);
    }

    @Test
    void testD_findIngredients_3_5() {
        System.out.println("------------- Test D -------------");
        List<Ingredient> ingredientsD = dataRetriever.findIngredients(3, 5);

        assertNotNull(ingredientsD);
        assertFalse(ingredientsD.isEmpty());

        printIngredients(ingredientsD);
    }

    @Test
    void testE_findDishsByIngredientName() {
        System.out.println("------------- Test E -------------");
        List<Dish> dishesE = dataRetriever.findDishsByIngredientName("laitue");

        assertNotNull(dishesE);
        assertFalse(dishesE.isEmpty());

        printDishes(dishesE);
    }

    @Test
    void testF_findIngredientsByCriteria_category() {
        System.out.println("------------- Test F -------------");
        List<Ingredient> ingredientsF =
                dataRetriever.findIngredientsByCriteria(
                        null,
                        CategoryEnum.VEGETABLE,
                        null,
                        1,
                        10
                );

        assertNotNull(ingredientsF);
        assertFalse(ingredientsF.isEmpty());

        printIngredients(ingredientsF);
    }

    @Test
    void testG_findIngredientsByCriteria_name_and_dish() {
        System.out.println("------------- Test G -------------");
        List<Ingredient> ingredientsG =
                dataRetriever.findIngredientsByCriteria(
                        "cho",
                        null,
                        "Sal",
                        1,
                        10
                );

        assertNotNull(ingredientsG);
        assertFalse(ingredientsG.isEmpty());

        printIngredients(ingredientsG);
    }

    @Test
    void testH_findIngredientsByCriteria_noResult() {
        System.out.println("------------- Test H -------------");
        List<Ingredient> ingredientsH =
                dataRetriever.findIngredientsByCriteria(
                        "cho",
                        null,
                        "gâteau",
                        1,
                        10
                );

        assertNotNull(ingredientsH);
        printIngredients(ingredientsH);
    }

    @Test
    void testI_createIngredients_existingId_shouldFail() {
        System.out.println("--- Test I ---");

        List<Ingredient> newIngredients1 = List.of(
                new Ingredient(1, "Laitue", 1200.0, CategoryEnum.VEGETABLE)
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> dataRetriever.createIngredients(newIngredients1)
        );

        System.out.println("Test 1 échoué : " + exception.getMessage());
    }

    @Test
    void testJ_createIngredients_success() {
        System.out.println("--- Test J ---");

        List<Ingredient> newIngredients2 = List.of(
                new Ingredient(null, "Poivron", 1200.0, CategoryEnum.OTHER),
                new Ingredient(null, "Viande de Porc", 15000.0, CategoryEnum.ANIMAL)
        );

        List<Ingredient> created = assertDoesNotThrow(
                () -> dataRetriever.createIngredients(newIngredients2)
        );

        System.out.println("Test 1 réussi : ingrédients créés :");
        for (Ingredient ing : created) {
            assertNotNull(ing.getId());
            System.out.println("- " + ing.getName() + " (Id=" + ing.getId() + ")");
        }
    }

    // =======================
    // TESTS COMMENTÉS (CONSERVÉS À L’IDENTIQUE)
    // =======================

    /*
    @Test
    void saveDish_test() {
        DishIngredient di1 = new DishIngredient();
        di1.setIngredient(new Ingredient(1, "gisa", 3000.0, CategoryEnum.OTHER));
        di1.setQuantity(1.0);
        di1.setUnit(Unit.KG);

        DishIngredient di2 = new DishIngredient();
        di2.setId(2);
        di2.setIngredient(new Ingredient(5, "Farine", 1200.0, CategoryEnum.OTHER));
        di2.setQuantity(1.0);
        di2.setUnit(Unit.KG);

        DishIngredient di3 = new DishIngredient();
        di3.setIngredient(new Ingredient(5, "Beurre", 2500.0, CategoryEnum.DAIRY));
        di3.setQuantity(1.0);
        di3.setUnit(Unit.KG);

        DishIngredient di4 = new DishIngredient();
        di4.setIngredient(new Ingredient(1, "Laitue", 800.0, CategoryEnum.VEGETABLE));
        di4.setQuantity(1.0);
        di4.setUnit(Unit.KG);

        Dish newDish = new Dish();
        newDish.setId(3);
        newDish.setName("Riz aux légumes");
        newDish.setDishType(DishTypeEnum.MAIN);
        newDish.setPrice(10000.0);
        newDish.setIngredients(List.of(di1, di2, di3, di4));

        Dish savedDish = dataRetriever.saveDish(newDish);
        savedDish.prettyPrint();
    }
    */

    @Test
    void testGetStockValueAt() {
        System.out.println("------- GetStockValues -------");

        Ingredient ingredientInStock = dataRetriever.findIngredientById(5);
        assertNotNull(ingredientInStock);

        Instant t = Instant.parse("2024-01-06T12:00:00Z");
        StockValue stock = ingredientInStock.getStockValueAt(t);

        assertNotNull(stock);
        assertNotNull(stock.getQuantity());
        assertNotNull(stock.getUnit());

        System.out.println(
                ingredientInStock.getName() +
                        " en stock est : " +
                        stock.getQuantity() + " " +
                        stock.getUnit()
        );
    }

    // =======================
    // MÉTHODES UTILITAIRES
    // =======================

    private void printIngredients(List<Ingredient> ingredients) {
        System.out.println("🥬 Ingrédients");
        System.out.println("---------------------------------");

        if (ingredients == null || ingredients.isEmpty()) {
            System.out.println("⚠️ Aucun ingrédient trouvé !");
        } else {
            for (Ingredient ing : ingredients) {
                System.out.println("️⃣ " + ing.getName());
                ing.prettyPrint();
                System.out.println();
            }
        }

        System.out.println("---------------------------------");
    }

    private void printDishes(List<Dish> dishes) {
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
