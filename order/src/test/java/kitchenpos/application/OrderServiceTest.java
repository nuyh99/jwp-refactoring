package kitchenpos.application;

import kitchenpos.OrderFixtures;
import kitchenpos.OrderTableFixtures;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.repository.OrderRepository;
import kitchenpos.domain.repository.OrderTableRepository;
import kitchenpos.dto.OrderCreateRequest;
import kitchenpos.dto.OrderLineItemCreateRequest;
import kitchenpos.dto.OrderResponse;
import kitchenpos.dto.OrderUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderTableRepository orderTableRepository;

    @Nested
    @DisplayName("주문 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("주문을 생성할 수 있다")
        void create() {
            //given
            final OrderTable orderTable = orderTableRepository.save(OrderTableFixtures.createWithNotEmpty());
            final OrderLineItemCreateRequest orderLineItem = new OrderLineItemCreateRequest(1L, 2);

            final OrderCreateRequest request = new OrderCreateRequest(orderTable.getId(), List.of(orderLineItem));

            //when
            final OrderResponse order = orderService.create(request);

            //then
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(order.getId()).isNotNull();
                softAssertions.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COOKING);
                softAssertions.assertThat(order.getOrderedTime()).isNotNull();
            });
        }

        @Test
        @DisplayName("주문을 생성할 때 주문 항목이 없으면 예외가 발생한다")
        void create_fail1() {
            //given
            final OrderTable orderTable = orderTableRepository.save(OrderTableFixtures.createWithNotEmpty());

            final OrderCreateRequest request = new OrderCreateRequest(orderTable.getId(), emptyList());

            //when, then
            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문 항목이 필요합니다.");
        }

        @Test
        @DisplayName("주문을 생성할 때 주문 항목 메뉴의 개수와 실제 존재하는 메뉴의 개수가 다르면 예외가 발생한다")
        void create_fail2() {
            //given
            final OrderTable orderTable = orderTableRepository.save(OrderTableFixtures.createWithNotEmpty());
            final OrderLineItemCreateRequest orderLineItem = new OrderLineItemCreateRequest(0L, 2);

            final OrderCreateRequest request = new OrderCreateRequest(orderTable.getId(), List.of(orderLineItem));

            //when, then
            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 주문 항목입니다.");
        }

        @Test
        @DisplayName("주문을 생성할 때 주문 테이블이 존재하지 않으면 예외가 발생한다")
        void create_fail3() {
            //given
            final OrderLineItemCreateRequest orderLineItem = new OrderLineItemCreateRequest(1L, 2);

            final OrderCreateRequest request = new OrderCreateRequest(0L, List.of(orderLineItem));

            //when, then
            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 주문 테이블입니다.");
        }

        @Test
        @DisplayName("주문을 생성할 때 주문 테이블이 빈 테이블이면 예외가 발생한다")
        void create_fail4() {
            //given
            final OrderTable orderTable = orderTableRepository.save(OrderTableFixtures.create());
            final OrderLineItemCreateRequest orderLineItem = new OrderLineItemCreateRequest(1L, 2);

            final OrderCreateRequest request = new OrderCreateRequest(orderTable.getId(), List.of(orderLineItem));

            //when, then
            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("빈 주문 테이블입니다.");
        }
    }

    @Test
    @DisplayName("주문 전체 조회를 할 수 있다")
    void list() {
        assertDoesNotThrow(() -> orderService.list());
    }

    @Nested
    @DisplayName("주문 상태 변경 테스트")
    class ChangeOrderStatusTest {

        @ParameterizedTest
        @EnumSource(OrderStatus.class)
        @DisplayName("주문 상태를 바꿀 수 있다")
        void changeOrderStatus(final OrderStatus orderStatus) {
            //given
            final Order order = orderRepository.save(OrderFixtures.create(1L, 1L));
            final OrderUpdateRequest request = new OrderUpdateRequest(orderStatus);

            //when
            final OrderResponse saved = orderService.changeOrderStatus(order.getId(), request);

            //then
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(saved.getId()).isEqualTo(order.getId());
                softAssertions.assertThat(saved.getOrderStatus()).isEqualTo(orderStatus);
            });
        }

        @Test
        @DisplayName("주문 상태를 바꾸려고 할 때 주문이 존재하지 않으면 예외가 발생한다")
        void changeOrderStatus_fail2() {
            //given
            final OrderUpdateRequest request = new OrderUpdateRequest(OrderStatus.MEAL);

            //when, then
            assertThatThrownBy(() -> orderService.changeOrderStatus(0L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 주문입니다.");
        }

        @Test
        @DisplayName("주문 상태를 바꾸려고 할 때 COMPLETION 상태의 주문이라면 예외가 발생한다")
        void changeOrderStatus_fail3() {
            //given
            final Order order = orderRepository.save(OrderFixtures.create(1L, 1L));
            final OrderUpdateRequest requestToCompletion = new OrderUpdateRequest(OrderStatus.COMPLETION);
            orderService.changeOrderStatus(order.getId(), requestToCompletion);

            final OrderUpdateRequest request = new OrderUpdateRequest(OrderStatus.MEAL);

            //when, then
            assertThatThrownBy(() -> orderService.changeOrderStatus(order.getId(), request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("완료된 주문은 변경할 수 없습니다.");
        }
    }
}
