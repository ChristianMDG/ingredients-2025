import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDateTime;
    private List<DishOrder> dishOrders;

    public Order(){}
    public Order(Integer id, String reference, Instant creationDateTime, List<DishOrder> dishOrders) {
        this.id = id;
        this.reference = reference;
        this.creationDateTime = creationDateTime;
        this.dishOrders = dishOrders;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) && Objects.equals(reference, order.reference) && Objects.equals(creationDateTime, order.creationDateTime) && Objects.equals(dishOrders, order.dishOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, creationDateTime, dishOrders);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", creationDateTime=" + creationDateTime +
                ", dishOrders=" + dishOrders +
                '}';
    }

    public void prettyPrint() {
        System.out.println("=================================");
        System.out.println("Commande : " + reference);
        System.out.println("ID       : " + id);
        System.out.println("Créée le : " + creationDateTime);
        System.out.println("=================================");
        System.out.println("Plats commandés :");

        for (DishOrder dishOrder : dishOrders) {
            Dish dish = dishOrder.getDish();
            System.out.println("-------------------------------------------------");
            System.out.println("Plat ID    : " + dish.getId());
            System.out.println("Nom        : " + dish.getName());
            System.out.println("Type       : " + dish.getDishType());
            System.out.println("Prix       : " + dish.getPrice());
            System.out.println("Quantité   : " + dishOrder.getQuantity());

            if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
                System.out.println("Ingrédients :");
                for (DishIngredient di : dish.getIngredients()) {
                    Ingredient ing = di.getIngredient();
                    System.out.println("  - " + ing.getName() + " (" + ing.getCategory() + ") : "
                            + di.getQuantity() + " " + di.getUnit());
                }
            } else {
                System.out.println("Ingrédients : Aucun");
            }
            System.out.println("-------------------------------------------------");
        }
        System.out.println("=================================");
    }

}
