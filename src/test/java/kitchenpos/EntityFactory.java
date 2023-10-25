package kitchenpos;

import kitchenpos.menu.domain.repository.MenuGroupRepository;
import kitchenpos.menu.domain.repository.MenuRepository;
import kitchenpos.order.domain.repository.OrderRepository;
import kitchenpos.table.domain.repository.OrderTableRepository;
import kitchenpos.product.domain.repository.ProductRepository;
import kitchenpos.tablegroup.domain.repository.TableGroupRepository;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.product.domain.Price;
import kitchenpos.product.domain.Product;
import kitchenpos.tablegroup.domain.TableGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.singletonList;

@Component
public class EntityFactory {

    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderTableRepository orderTableRepository;
    @Autowired
    private MenuGroupRepository menuGroupRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TableGroupRepository tableGroupRepository;

    public OrderTable saveOrderTable() {
        final OrderTable request = new OrderTable(5, true);
        return orderTableRepository.save(request);
    }

    public OrderTable saveOrderTableWithNotEmpty() {
        final OrderTable request = new OrderTable(5, false);
        return orderTableRepository.save(request);
    }

    public TableGroup saveTableGroup(final OrderTable orderTable1, final OrderTable orderTable2) {
        final TableGroup tableGroup = new TableGroup(List.of(orderTable1, orderTable2));
        return tableGroupRepository.save(tableGroup);
    }

    public Order saveOrder(final OrderTable orderTable) {
        final Menu menu = saveMenu();
        final OrderLineItem orderLineItem = new OrderLineItem(menu.getId(), 2);

        final Order request = new Order(orderTable, List.of(orderLineItem));
        return orderRepository.save(request);
    }

    public Order saveOrder() {
        final OrderTable orderTable = saveOrderTableWithNotEmpty();
        final Menu menu = saveMenu();
        final OrderLineItem orderLineItem = new OrderLineItem(menu.getId(), 2);

        final Order request = new Order(orderTable, List.of(orderLineItem));
        return orderRepository.save(request);
    }

    public Menu saveMenu() {
        final Product product = saveProduct("연어", 4000);
        final MenuProduct menuProduct = new MenuProduct(product, 4);
        final MenuGroup menuGroup = saveMenuGroup("일식");

        final Menu request = new Menu("떡볶이 세트", new Price(BigDecimal.valueOf(16000)), menuGroup.getId(),
                singletonList(menuProduct));

        return menuRepository.save(request);
    }

    public Product saveProduct(final String name, final int value) {
        final Price price = new Price(BigDecimal.valueOf(value));
        final Product product = new Product(name, price);
        return productRepository.save(product);
    }

    public MenuGroup saveMenuGroup(final String name) {
        final MenuGroup menuGroup = new MenuGroup(name);
        return menuGroupRepository.save(menuGroup);
    }
}
