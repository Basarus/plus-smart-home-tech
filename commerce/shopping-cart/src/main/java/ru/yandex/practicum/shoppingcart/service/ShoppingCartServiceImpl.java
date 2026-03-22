package ru.yandex.practicum.shoppingcart.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.client.WarehouseGateway;
import ru.yandex.practicum.shoppingcart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.shoppingcart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.shoppingcart.exception.ShoppingCartDeactivatedException;
import ru.yandex.practicum.shoppingcart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.shoppingcart.model.ShoppingCart;
import ru.yandex.practicum.shoppingcart.model.ShoppingCartItem;
import ru.yandex.practicum.shoppingcart.model.ShoppingCartStatus;
import ru.yandex.practicum.shoppingcart.repository.ShoppingCartRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseGateway warehouseGateway;

    @Override
    @Transactional(readOnly = true)
    public ShoppingCartDto getShoppingCart(String username) {
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createTransientEmptyCart(username));

        return shoppingCartMapper.toDto(cart);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createNewCart(username));

        ensureActive(cart);

        Map<UUID, Long> merged = new HashMap<>(cart.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProductId(), item -> item.getQuantity())));

        products.forEach((productId, quantity) -> merged.merge(productId, quantity, Long::sum));

        warehouseGateway.checkProducts(new ShoppingCartDto(cart.getShoppingCartId(), merged));

        Map<UUID, ShoppingCartItem> currentItems = cart.getItems().stream()
                .collect(Collectors.toMap(item -> item.getProductId(), Function.identity()));

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            ShoppingCartItem item = currentItems.get(entry.getKey());
            if (item == null) {
                item = new ShoppingCartItem();
                item.setProductId(entry.getKey());
                item.setQuantity(entry.getValue());
                item.setShoppingCart(cart);
                cart.getItems().add(item);
            } else {
                item.setQuantity(item.getQuantity() + entry.getValue());
            }
        }

        ShoppingCart saved = shoppingCartRepository.save(cart);
        return shoppingCartMapper.toDto(saved);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createNewCart(username));

        cart.setStatus(ShoppingCartStatus.DEACTIVATE);
        shoppingCartRepository.save(cart);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createTransientEmptyCart(username));

        List<UUID> existingIds = cart.getItems().stream()
                .map(item -> item.getProductId())
                .toList();

        boolean hasAny = products.stream().anyMatch(existingIds::contains);
        if (!hasAny) {
            throw new NoProductsInShoppingCartException();
        }

        cart.getItems().removeIf(item -> products.contains(item.getProductId()));

        ShoppingCart saved = shoppingCartRepository.save(cart);
        return shoppingCartMapper.toDto(saved);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        validateUsername(username);

        ShoppingCart cart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createTransientEmptyCart(username));

        ensureActive(cart);

        ShoppingCartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.productId()))
                .findFirst()
                .orElseThrow(NoProductsInShoppingCartException::new);

        Map<UUID, Long> merged = new HashMap<>(cart.getItems().stream()
                .collect(Collectors.toMap(i -> i.getProductId(), i -> i.getQuantity())));
        merged.put(request.productId(), request.newQuantity());

        warehouseGateway.checkProducts(new ShoppingCartDto(cart.getShoppingCartId(), merged));

        item.setQuantity(request.newQuantity());

        ShoppingCart saved = shoppingCartRepository.save(cart);
        return shoppingCartMapper.toDto(saved);
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
    }

    private void ensureActive(ShoppingCart cart) {
        if (cart.getStatus() == ShoppingCartStatus.DEACTIVATE) {
            throw new ShoppingCartDeactivatedException();
        }
    }

    private ShoppingCart createNewCart(String username) {
        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartId(UUID.randomUUID());
        cart.setUsername(username);
        cart.setStatus(ShoppingCartStatus.ACTIVE);
        cart.setItems(new ArrayList<>());
        return shoppingCartRepository.save(cart);
    }

    private ShoppingCart createTransientEmptyCart(String username) {
        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartId(UUID.randomUUID());
        cart.setUsername(username);
        cart.setStatus(ShoppingCartStatus.ACTIVE);
        cart.setItems(new ArrayList<>());
        return cart;
    }
}