import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement>  stockMovementList;

    public Ingredient() {}
    public Ingredient(String name, Double price, CategoryEnum category, List<StockMovement> stockMovementList) {
        this.name = name;
        this.price = price;
        this.category = category;
    }
    public Ingredient(Integer id, String name, Double price, CategoryEnum category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public StockValue getStockValueAt(Instant t) {

        double stock = 0.0;

        if (stockMovementList != null) {
            for (StockMovement movement : stockMovementList) {

                if (movement.getCreationDateTime() != null
                        && movement.getValue() != null
                        && movement.getValue().getQuantity() != null
                        && !movement.getCreationDateTime().isAfter(t)) {

                    double quantity = movement.getValue().getQuantity();

                    if (movement.getType() == MovementTypeEnum.OUT) {
                        quantity = -quantity;
                    }

                    stock += quantity;
                }
            }
        }

        return new StockValue(stock, Unit.KG);
    }




    public void prettyPrint() {
        System.out.println("   - ID        : " + id);
        System.out.println("   - Catégorie : " + category);
        System.out.println("   - Prix      : " + price + " Ar");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(price, that.price) && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, category);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                '}';
    }
}
