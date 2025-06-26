package bookstore.model.enumeration;

import bookstore.model.Order;
import java.text.MessageFormat;

public enum OrderStatus {
    NEW {
        @Override
        public void handleCancel(Order order) {
            order.setStatus(CANCELLED);
        }
    },
    PROCESSED {
        @Override
        public void handleCancel(Order order) {
            throw new IllegalStateException(CONTACT_SUPPORT_MESSAGE);
        }
    },
    SHIPPED {
        @Override
        public void handleCancel(Order order) {
            throw new IllegalStateException(CONTACT_SUPPORT_MESSAGE);
        }
    },
    DELIVERED {
        @Override
        public void handleCancel(Order order) {
            throw new IllegalStateException(CONTACT_SUPPORT_MESSAGE);
        }
    },
    CANCELLED {
        @Override
        public void handleCancel(Order order) {
            throw new IllegalStateException(MessageFormat.format(
                    ORDER_ALREADY_CANCELLED_MESSAGE, order.getId()));
        }
    };

    private static final String CONTACT_SUPPORT_MESSAGE = "Please contact our support";
    private static final String ORDER_ALREADY_CANCELLED_MESSAGE =
            "Order with id {0} has already been cancelled";

    public abstract void handleCancel(Order order);
}
