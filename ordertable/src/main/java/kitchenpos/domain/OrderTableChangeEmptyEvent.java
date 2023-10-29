package kitchenpos.domain;

public class OrderTableChangeEmptyEvent {

    private final Long orderTableId;

    public OrderTableChangeEmptyEvent(final Long orderTableId) {
        this.orderTableId = orderTableId;
    }

    public Long getOrderTableId() {
        return orderTableId;
    }
}
