package com.example.service;

import com.example.common.Constants;
import com.example.common.enums.RoleEnum;
import com.example.controller.request.AddCartItemRequest;
import com.example.controller.request.PlaceOrderRequest;
import com.example.entity.Account;
import com.example.entity.CustomerOrder;
import com.example.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = {com.example.SpringbootApplication.class, CartCheckoutTransactionIntegrationTest.ControlledOrderServiceConfiguration.class},
        properties = {
                "spring.datasource.url=jdbc:h2:mem:cart_checkout;MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.hikari.transaction-isolation=TRANSACTION_REPEATABLE_READ",
                "spring.flyway.enabled=false",
                "spring.main.allow-bean-definition-overriding=true"
        })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartCheckoutTransactionIntegrationTest {
    @Autowired private CartService cartService;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ControlledOrderService orderService;

    private ExecutorService executor;

    @BeforeAll
    void createSchema() {
        jdbcTemplate.execute("create table if not exists user (id int primary key, username varchar(64), password varchar(100), name varchar(100), role varchar(32))");
        jdbcTemplate.execute("create table if not exists business (id int primary key, name varchar(100), status varchar(32))");
        jdbcTemplate.execute("create table if not exists type (id int primary key, name varchar(100))");
        jdbcTemplate.execute("create table if not exists goods (id int primary key, name varchar(160), description varchar(1000), img varchar(500), price decimal(12, 2), unit varchar(32), count int, type_id int, business_id int)");
        jdbcTemplate.execute("create table if not exists cart_item (id int auto_increment primary key, user_id int not null, goods_id int not null, quantity int not null, created_at timestamp default current_timestamp, updated_at timestamp default current_timestamp)");
        jdbcTemplate.execute("create table if not exists customer_order (id int auto_increment primary key, order_number varchar(64) unique, goods_id int, user_id int, business_id int, product_name varchar(160), product_img varchar(500), quantity int, unit_price decimal(12, 2), total_price decimal(12, 2), status varchar(32), created_at timestamp default current_timestamp, updated_at timestamp default current_timestamp)");
    }

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.update("delete from customer_order");
        jdbcTemplate.update("delete from cart_item");
        jdbcTemplate.update("delete from goods");
        jdbcTemplate.update("delete from type");
        jdbcTemplate.update("delete from business");
        jdbcTemplate.update("delete from user");
        jdbcTemplate.update("insert into user (id, username, password, name, role) values (1, 'customer', 'unused', 'Customer', 'USER')");
        jdbcTemplate.update("insert into goods (id, name, img, price, count) values (1, 'First product', 'first.webp', 10.00, 10)");
        jdbcTemplate.update("insert into goods (id, name, img, price, count) values (2, 'Second product', 'second.webp', 20.00, 10)");
        orderService.reset();
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void cleanUp() {
        if (executor != null) {
            executor.shutdownNow();
        }
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void addStartedDuringPausedCheckoutWaitsForTheCustomerLockAndSurvivesCartClear() throws Exception {
        jdbcTemplate.update("insert into cart_item (user_id, goods_id, quantity) values (1, 1, 1)");
        CountDownLatch firstOrderPersisted = new CountDownLatch(1);
        CountDownLatch resumeCheckout = new CountDownLatch(1);
        orderService.pauseAfterFirstOrder(firstOrderPersisted, resumeCheckout);

        Future<List<CustomerOrder>> checkout = executor.submit(() -> asCustomer(cartService::checkout));
        assertTrue(firstOrderPersisted.await(5, TimeUnit.SECONDS));

        CountDownLatch addStarted = new CountDownLatch(1);
        Future<?> add = executor.submit(() -> {
            addStarted.countDown();
            return asCustomer(() -> cartService.add(new AddCartItemRequest(1, 1)));
        });
        assertTrue(addStarted.await(5, TimeUnit.SECONDS));
        assertThrows(TimeoutException.class, () -> add.get(500, TimeUnit.MILLISECONDS));

        resumeCheckout.countDown();
        assertEquals(1, checkout.get(5, TimeUnit.SECONDS).size());
        add.get(5, TimeUnit.SECONDS);

        assertEquals(1L, count("select count(*) from customer_order"));
        assertEquals(9, stock(1));
        assertEquals(10, stock(2));
        assertEquals(1L, count("select count(*) from cart_item where user_id = 1 and goods_id = 1 and quantity = 1"));
    }

    @Test
    void aLaterCheckoutFailureRollsBackEarlierRealOrderStockAndEveryCartLine() {
        jdbcTemplate.update("insert into cart_item (user_id, goods_id, quantity) values (1, 2, 3)");
        jdbcTemplate.update("insert into cart_item (user_id, goods_id, quantity) values (1, 1, 2)");
        orderService.failAfterFirstOrderWhenGoodsId(2);

        assertThrows(CustomException.class, () -> asCustomer(cartService::checkout));

        assertTrue(orderService.firstOrderPlaced());
        assertEquals(0L, count("select count(*) from customer_order"));
        assertEquals(10, stock(1));
        assertEquals(10, stock(2));
        assertEquals(1L, count("select count(*) from cart_item where user_id = 1 and goods_id = 1 and quantity = 2"));
        assertEquals(1L, count("select count(*) from cart_item where user_id = 1 and goods_id = 2 and quantity = 3"));
    }

    private long count(String sql) {
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private int stock(int goodsId) {
        return jdbcTemplate.queryForObject("select count from goods where id = ?", Integer.class, goodsId);
    }

    private <T> T asCustomer(ThrowingSupplier<T> action) throws Exception {
        Account account = new Account();
        account.setId(1);
        account.setRole(RoleEnum.USER.name());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(Constants.CURRENT_USER, account);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            return action.get();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ControlledOrderServiceConfiguration {
        @Bean(name = "orderService")
        @Primary
        ControlledOrderService orderService() {
            return new ControlledOrderService();
        }
    }

    static class ControlledOrderService extends OrderService {
        private CountDownLatch pauseAfterFirstOrder;
        private CountDownLatch resumeCheckout;
        private volatile boolean firstOrderPlaced;
        private volatile Integer failWhenGoodsId;

        @Override
        public CustomerOrder placeOrder(PlaceOrderRequest request) {
            if (firstOrderPlaced && request.goodsId().equals(failWhenGoodsId)) {
                throw new CustomException("5999", "Injected later checkout failure");
            }
            CustomerOrder order = super.placeOrder(request);
            firstOrderPlaced = true;
            if (pauseAfterFirstOrder != null) {
                pauseAfterFirstOrder.countDown();
                await(resumeCheckout);
            }
            return order;
        }

        void reset() {
            pauseAfterFirstOrder = null;
            resumeCheckout = null;
            firstOrderPlaced = false;
            failWhenGoodsId = null;
        }

        void pauseAfterFirstOrder(CountDownLatch firstOrderPersisted, CountDownLatch resumeCheckout) {
            this.pauseAfterFirstOrder = firstOrderPersisted;
            this.resumeCheckout = resumeCheckout;
        }

        void failAfterFirstOrderWhenGoodsId(Integer goodsId) {
            this.failWhenGoodsId = goodsId;
        }

        boolean firstOrderPlaced() {
            return firstOrderPlaced;
        }

        private void await(CountDownLatch latch) {
            try {
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting for the checkout test to resume");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Checkout test interrupted", exception);
            }
        }
    }
}
