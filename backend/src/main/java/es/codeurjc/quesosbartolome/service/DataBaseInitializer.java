package es.codeurjc.quesosbartolome.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.codeurjc.quesosbartolome.model.Cart;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.Invoice;
import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.model.OrderItem;
import es.codeurjc.quesosbartolome.model.Review;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.InvoiceRepository;
import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.repository.ReviewRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service
public class DataBaseInitializer {

        @Autowired
        private CheeseRepository cheeseRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ReviewRepository reviewRepository;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private InvoiceRepository invoiceRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        public Blob saveImage(String resourcePath) throws IOException {
                ClassLoader classLoader = getClass().getClassLoader();
                try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
                        if (inputStream == null) {
                                throw new IOException("File not found in classpath: " + resourcePath);
                        }

                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] data = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                                buffer.write(data, 0, bytesRead);
                        }

                        return BlobProxy.generateProxy(buffer.toByteArray());
                }
        }

        List<Double> inventoryA = List.of(
                        5.12, 5.34, 5.48, 5.55, 5.63,
                        5.77, 5.89, 6.02, 6.15, 6.21,
                        6.33, 6.41, 6.52, 6.60, 6.71,
                        6.80, 6.85, 6.90, 6.95, 6.99,
                        6.45, 5.98, 6.27, 5.76, 6.58);

        List<Double> inventoryB = List.of(
                        4.05, 4.12, 4.20, 4.28, 4.33,
                        4.41, 4.50, 4.57, 4.63, 4.70,
                        4.78, 4.85, 4.92, 4.99, 5.05,
                        5.12, 5.18, 5.23, 5.30, 5.37,
                        4.66, 4.89, 4.74, 5.33, 4.58);

        private User createUser(String name, String gmail, String direction, String nif) throws IOException {
                User user = new User();
                user.setName(name);
                user.setPassword(passwordEncoder.encode("password123"));
                user.setGmail(gmail);
                user.setDirection(direction);
                user.setNif(nif);
                user.setRols("USER");
                user.setCart(new Cart(user));
                user.setImage(saveImage("images/default-profile.jpg"));
                return user;
        }

        private void addReview(User user, Cheese cheese, int rating, String comment) {
                Review review = new Review(rating, comment, user, cheese);
                reviewRepository.save(review);
                user.getReviews().add(review);
                cheese.getReviews().add(review);
        }

        private List<Double> pickBoxes(Cheese cheese, int count, int seed) {
                List<Double> source = (cheese.getName().equals("Azul") || cheese.getName().equals("Chevrett"))
                                ? inventoryB
                                : inventoryA;

                List<Double> picked = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                        picked.add(source.get(Math.floorMod(seed + i * 3, source.size())));
                }
                return picked;
        }

        private Order createMixedOrder(User user, LocalDateTime orderDate, boolean processed, int seed,
                        Cheese[] cheeses, int[] boxCounts) {
                Order order = new Order(user);
                order.setTotalWeight(0.0);
                order.setTotalPrice(0.0);
                order.setProcessed(processed);
                order.setOrderDate(orderDate);

                double totalWeight = 0.0;
                double totalPrice = 0.0;

                for (int i = 0; i < cheeses.length; i++) {
                        Cheese cheese = cheeses[i];
                        int count = boxCounts[i];
                        List<Double> boxWeights = pickBoxes(cheese, count, seed + i * 11);

                        double itemWeight = round2(boxWeights.stream().mapToDouble(Double::doubleValue).sum());
                        double itemTotal = round2(cheese.getPrice() * itemWeight);

                        OrderItem item = new OrderItem(order,
                                        cheese.getId(),
                                        cheese.getName(),
                                        cheese.getPrice(),
                                        boxWeights,
                                        itemWeight,
                                        itemTotal);
                        order.getItems().add(item);

                        totalWeight = round2(totalWeight + itemWeight);
                        totalPrice = round2(totalPrice + itemTotal);
                }

                order.setTotalWeight(round2(totalWeight));
                order.setTotalPrice(round2(totalPrice));

                Order savedOrder = orderRepository.save(order);
                user.getOrders().add(savedOrder);
                userRepository.save(user);
                return savedOrder;
        }

        private void createInvoice(User user, Order order, double taxableBase, double totalPrice, LocalDateTime invoiceDate,
                        int invoiceNumber) {
                Invoice invoice = new Invoice(user, order);
                invoice.setTaxableBase(round2(taxableBase));
                invoice.setTotalPrice(round2(totalPrice));
                invoice.setInvoiceDate(invoiceDate);

                Invoice savedInvoice = invoiceRepository.save(invoice);
                int yearSuffix = savedInvoice.getInvoiceDate().getYear() % 100;
                savedInvoice.setInvNo(String.format("FACT-Q%02d/%d", yearSuffix, invoiceNumber));
                invoiceRepository.save(savedInvoice);
        }

        private double round2(double value) {
                return Math.round(value * 100.0) / 100.0;
        }

        @PostConstruct
        public void init() throws IOException, URISyntaxException {
                // Create Cheese 1
                Cheese semicurado = new Cheese();
                semicurado.setName("Semicurado");
                semicurado.setPrice(17.50);
                semicurado.setDescription(
                                "Queso de pasta prensada madurado durante 21 días, con un sabor que comienza suave y cremoso pero se intensifica al final. Aromático sin resultar demasiado fuerte, ofrece un equilibrio agradable que lo hace fácil de disfrutar solo o acompañado.");
                semicurado.setManufactureDate(Date.valueOf(LocalDate.now().minusMonths(2)));
                semicurado.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(10)));
                semicurado.setType("Pasta prensada");
                semicurado.setBoxes(inventoryA);
                semicurado.setImage(saveImage("images/image-Semicurado.JPG"));

                // Create Cheese 2
                Cheese azul = new Cheese();
                azul.setName("Azul");
                azul.setPrice(15.00);
                azul.setDescription(
                                "Queso azul de tradición clásica, de pasta prensada y curación cuidada, con vetas de moho que aportan un sabor intenso y profundo. Su textura es cremosa y su carácter es marcado sin llegar a ser excesivamente agresivo, ofreciendo ese equilibrio típico del azul de toda la vida.");
                azul.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(3)));
                azul.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(2)));
                azul.setType("Maduración fúngica");
                azul.setBoxes(inventoryB);
                azul.setImage(saveImage("images/image-Azul.JPG"));

                // Create Cheese 3
                Cheese curado = new Cheese();
                curado.setName("Curado");
                curado.setPrice(17.50);
                curado.setDescription(
                                "Queso de pasta prensada madurado durante mes y medio, con una textura más firme y un sabor más desarrollado. Mantiene un carácter equilibrado: comienza suave pero gana intensidad sin llegar a ser excesivamente fuerte.");
                curado.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(5)));
                curado.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(3)));
                curado.setType("Pasta prensada");
                curado.setBoxes(inventoryA);
                curado.setImage(saveImage("images/image-Curado.JPG"));

                // Create Cheese 4
                Cheese chevrett = new Cheese();
                chevrett.setName("Chevrett");
                chevrett.setPrice(20.00);
                chevrett.setDescription(
                                "Queso de pasta blanda y textura cremosa, similar a un camembert pero con matices propios. Su curación suave resalta un sabor delicado que se vuelve más aromático con el tiempo, sin resultar fuerte, ideal para quienes disfrutan de quesos tiernos pero con personalidad.");
                chevrett.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(2)));
                chevrett.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(1).plusWeeks(1)));
                chevrett.setType("Cremoso");
                chevrett.setBoxes(List.of());
                chevrett.setImage(saveImage("images/image-Chevrett.JPG"));

                // Create Cheese 5
                Cheese tierno = new Cheese();
                tierno.setName("Tierno");
                tierno.setPrice(18.00);
                tierno.setDescription(
                                "Queso de pasta prensada madurado durante una semana, de textura muy suave y húmeda. Presenta un sabor ligero y lácteo, nada fuerte, ideal para quienes prefieren quesos delicados y fáciles de comer");
                tierno.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(2)));
                tierno.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(1).plusWeeks(1)));
                tierno.setType("Pasta prensada");
                tierno.setBoxes(inventoryA);
                tierno.setImage(saveImage("images/image-Tierno.JPG"));

                // Create user 1
                User user1 = new User();
                user1.setName("Victor");
                user1.setPassword(passwordEncoder.encode("password123"));
                user1.setGmail("victor@example.com");
                user1.setDirection("123 Main St");
                user1.setNif("12345678A");
                user1.setRols("USER");
                user1.setCart(new Cart(user1));
                user1.setImage(saveImage("images/default-profile.jpg"));

                // Create user 2
                User user2 = new User();
                user2.setName("User");
                user2.setPassword(passwordEncoder.encode("password123"));
                user2.setGmail("user@example.com");
                user2.setDirection("123 Main St");
                user2.setNif("12345678A");
                user2.setRols("USER");
                user2.setCart(new Cart(user2));
                user2.setImage(saveImage("images/default-profile.jpg"));

                // Create Admin 1
                User userAdmin = new User();
                userAdmin.setName("German");
                userAdmin.setPassword(passwordEncoder.encode("password123"));
                userAdmin.setGmail("german@example.com");
                userAdmin.setDirection("123 Main St");
                userAdmin.setNif("12345678A");
                userAdmin.setRols("ADMIN");
                userAdmin.setImage(saveImage("images/default-profile.jpg"));

                // Create Admin 2
                User userAdmin2 = new User();
                userAdmin2.setName("Admin");
                userAdmin2.setPassword(passwordEncoder.encode("password123"));
                userAdmin2.setGmail("Admin@example.com");
                userAdmin2.setDirection("123 Main St");
                userAdmin2.setNif("12345678A");
                userAdmin2.setRols("ADMIN");
                userAdmin2.setImage(saveImage("images/default-profile.jpg"));

                // Create additional customers so the application opens with realistic activity.
                User tiendaRiaza = createUser("Tienda Artesanal de Riaza", "riaza@ejemplo.com", "Plaza Mayor, Riaza", "70000001A");
                tiendaRiaza.setImage(saveImage("images/image-tienda1.avif"));
                User supermercadoAldeonte = createUser("Supermercado Aldeonte", "aldeonte@ejemplo.com", "Calle Real, Aldeonte", "70000002B");
                supermercadoAldeonte.setImage(saveImage("images/image-tienda2.webp"));
                User queseriaAyllon = createUser("Queseria Ayllon", "ayllon@ejemplo.com", "Ayllon", "70000003C");
                queseriaAyllon.setImage(saveImage("images/image-tienda3.webp"));
                User colmadoSepulveda = createUser("Colmado Sepulveda", "sepulveda@ejemplo.com", "Sepulveda", "70000004D");
                colmadoSepulveda.setImage(saveImage("images/image-tienda4.png"));
                User tiendaMaderuelo = createUser("Tienda de Maderuelo", "maderuelo@ejemplo.com", "Maderuelo", "70000005E");
                tiendaMaderuelo.setImage(saveImage("images/image-tienda5.jpg"));
                User mercadoCantalejo = createUser("Mercado de Cantalejo", "cantalejo@ejemplo.com", "Cantalejo", "70000006F");
                mercadoCantalejo.setImage(saveImage("images/image-tienda6.avif"));
                User distribucionesBurgomillodo = createUser("Distribuciones Burgomillodo", "burgomillodo@ejemplo.com", "Burgomillodo", "70000007G");
                distribucionesBurgomillodo.setImage(saveImage("images/image-tienda7.webp"));
                User ecoValle = createUser("Eco Valle de Sepulveda", "ecovalle@ejemplo.com", "Valle de Sepulveda", "70000008H");
                ecoValle.setImage(saveImage("images/image-tienda8.jpg"));
                User gourmetPedraza = createUser("Gourmet Pedraza", "pedraza@ejemplo.com", "Pedraza", "70000009I");
                gourmetPedraza.setImage(saveImage("images/image-tienda9.jpg"));
                User charcuteriaBoceguillas = createUser("Charcuteria Boceguillas", "boceguillas@ejemplo.com", "Boceguillas", "70000010J");
                charcuteriaBoceguillas.setImage(saveImage("images/image-tienda10.jpg"));

                // Save cheeses to DB
                cheeseRepository.save(semicurado);
                cheeseRepository.save(azul);
                cheeseRepository.save(curado);
                cheeseRepository.save(chevrett);
                cheeseRepository.save(tierno);

                // Save users in DB
                userRepository.save(user1);
                userRepository.save(user2);
                userRepository.save(userAdmin);
                userRepository.save(userAdmin2);
                userRepository.save(tiendaRiaza);
                userRepository.save(supermercadoAldeonte);
                userRepository.save(queseriaAyllon);
                userRepository.save(colmadoSepulveda);
                userRepository.save(tiendaMaderuelo);
                userRepository.save(mercadoCantalejo);
                userRepository.save(distribucionesBurgomillodo);
                userRepository.save(ecoValle);
                userRepository.save(gourmetPedraza);
                userRepository.save(charcuteriaBoceguillas);

                // Seed reviews across all cheeses so the site opens with visible activity.
                addReview(user1, semicurado, 5,
                                "¡Excelente queso! Sabor equilibrado y textura perfecta. Ideal para tapas.");
                addReview(user2, semicurado, 4,
                                "Muy bueno, aunque esperaba un sabor un poco más intenso. Aún así lo recomiendo.");
                addReview(user1, semicurado, 5,
                                "Cremoso y aromático. Perfecto para acompañar con vino tinto. ¡Volveré a comprarlo!");
                addReview(user2, semicurado, 1, "Muy malo");

                addReview(tiendaRiaza, semicurado, 5, "Muy equilibrado, entra perfecto en una cesta gourmet de la sierra.");
                addReview(supermercadoAldeonte, azul, 4, "Para venta al público funciona muy bien, sabor potente pero comercial.");
                addReview(queseriaAyllon, curado, 5, "Producto serio y con carácter, ideal para mostrador de quesería tradicional.");
                addReview(colmadoSepulveda, chevrett, 4, "Muy cremoso y con buena presentación para un colmado de pueblo.");
                addReview(tiendaMaderuelo, tierno, 5, "Perfecto para familias y para tablas suaves de temporada.");
                addReview(mercadoCantalejo, semicurado, 4, "Se vende muy bien, buen punto entre suavidad y sabor.");
                addReview(distribucionesBurgomillodo, azul, 5, "Muy buen queso para distribución local y restauración.");
                addReview(ecoValle, curado, 4, "Encaja en una oferta de producto local con valor añadido.");
                addReview(gourmetPedraza, chevrett, 5, "Ideal para tienda gourmet y maridajes con vinos de la zona.");
                addReview(charcuteriaBoceguillas, tierno, 4, "Muy vendible, buen queso de entrada para mostrador.");

                addReview(tiendaRiaza, azul, 4, "Intenso y con personalidad, muy recomendable para escaparate gourmet.");
                addReview(supermercadoAldeonte, azul, 5, "Muy buen azul para venta al público y formatos de mostrador.");
                addReview(queseriaAyllon, curado, 5, "Sabor largo y elegante, encaja muy bien en quesería tradicional.");
                addReview(colmadoSepulveda, curado, 4, "Se adapta muy bien a raciones y tablas de colmado.");
                addReview(tiendaMaderuelo, chevrett, 5, "Cremoso y muy vistoso en escaparate, sale mucho en tienda.");
                addReview(mercadoCantalejo, chevrett, 4, "Gran textura para clientes que buscan algo suave.");
                addReview(distribucionesBurgomillodo, tierno, 5, "Delicado y fácil de recomendar para distribución local.");
                addReview(ecoValle, tierno, 4, "Buen queso de diario, funciona bien en una oferta de proximidad.");

                // Mass seed: orders and invoices from stores only, sorted oldest -> newest.
                List<User> storeUsers = List.of(
                                tiendaRiaza,
                                supermercadoAldeonte,
                                queseriaAyllon,
                                colmadoSepulveda,
                                tiendaMaderuelo,
                                mercadoCantalejo,
                                distribucionesBurgomillodo,
                                ecoValle,
                                gourmetPedraza,
                                charcuteriaBoceguillas);

                List<int[]> mixPatterns = List.of(
                                new int[] { 2, 2, 1 },
                                new int[] { 3, 1 },
                                new int[] { 1, 1, 1, 1, 1 },
                                new int[] { 2, 1, 2 },
                                new int[] { 4, 2 }
                );

                int invoiceCounter = 1;
                int entryIndex = 0;

                // Heavy seed: each store creates between 1 and 3 orders per month.
                // Full year 2025 + January to March 2026.
                for (int year = 2025; year <= 2026; year++) {
                        int lastMonth = (year == 2025) ? 12 : 3;

                        for (int month = 1; month <= lastMonth; month++) {
                                for (int storeIndex = 0; storeIndex < storeUsers.size(); storeIndex++) {
                                        User store = storeUsers.get(storeIndex);
                                        int ordersThisMonth = 1 + ((storeIndex + month + year) % 3); // 1..3

                                        for (int orderInMonth = 1; orderInMonth <= ordersThisMonth; orderInMonth++) {
                                                int patternIndex = (entryIndex + storeIndex + orderInMonth + month) % mixPatterns
                                                                .size();
                                                int[] counts = mixPatterns.get(patternIndex);

                                                Cheese[] orderCheeses;
                                                if (patternIndex == 0) {
                                                        orderCheeses = new Cheese[] { semicurado, azul, curado };
                                                } else if (patternIndex == 1) {
                                                        orderCheeses = new Cheese[] { chevrett, tierno };
                                                } else if (patternIndex == 2) {
                                                        orderCheeses = new Cheese[] { semicurado, azul, curado, chevrett,
                                                                        tierno };
                                                } else if (patternIndex == 3) {
                                                        orderCheeses = new Cheese[] { curado, semicurado, tierno };
                                                } else {
                                                        orderCheeses = new Cheese[] { azul, chevrett };
                                                }

                                                int day = 3 + (storeIndex % 4) + (orderInMonth - 1) * 9;
                                                LocalDateTime invoiceDate = LocalDateTime.of(
                                                                year,
                                                                month,
                                                                day,
                                                                9 + (storeIndex % 6),
                                                                10 + ((orderInMonth * 10) % 50));

                                                Order order = createMixedOrder(
                                                                store,
                                                                invoiceDate.minusDays(1),
                                                                true,
                                                                entryIndex,
                                                                orderCheeses,
                                                                counts);

                                                double total = order.getTotalPrice();
                                                double taxableBase = Math.round((total / 1.04) * 100.0) / 100.0;

                                                createInvoice(
                                                                store,
                                                                order,
                                                                taxableBase,
                                                                total,
                                                                invoiceDate,
                                                                invoiceCounter++);

                                                entryIndex++;
                                        }
                                }
                        }
                }

                // Pending orders left unprocessed so admin has a visible queue.
                createMixedOrder(tiendaMaderuelo, LocalDateTime.now().minusHours(8), false, 1001,
                                new Cheese[] { semicurado, curado, azul }, new int[] { 2, 1, 2 });
                createMixedOrder(mercadoCantalejo, LocalDateTime.now().minusHours(6), false, 1002,
                                new Cheese[] { chevrett, tierno }, new int[] { 3, 1 });
                createMixedOrder(distribucionesBurgomillodo, LocalDateTime.now().minusHours(4), false, 1003,
                                new Cheese[] { semicurado, azul, curado, chevrett, tierno },
                                new int[] { 1, 1, 1, 1, 1 });
                createMixedOrder(gourmetPedraza, LocalDateTime.now().minusHours(2), false, 1004,
                                new Cheese[] { curado, semicurado, tierno }, new int[] { 2, 1, 2 });
                createMixedOrder(charcuteriaBoceguillas, LocalDateTime.now().minusHours(1), false, 1005,
                                new Cheese[] { azul, chevrett }, new int[] { 4, 2 });
                createMixedOrder(tiendaRiaza, LocalDateTime.now().minusMinutes(50), false, 1006,
                                new Cheese[] { semicurado, azul, curado }, new int[] { 2, 2, 1 });
                createMixedOrder(supermercadoAldeonte, LocalDateTime.now().minusMinutes(40), false, 1007,
                                new Cheese[] { semicurado, azul, curado, chevrett, tierno },
                                new int[] { 1, 1, 1, 1, 1 });
                createMixedOrder(queseriaAyllon, LocalDateTime.now().minusMinutes(30), false, 1008,
                                new Cheese[] { chevrett, tierno }, new int[] { 3, 1 });
                createMixedOrder(colmadoSepulveda, LocalDateTime.now().minusMinutes(20), false, 1009,
                                new Cheese[] { curado, semicurado, tierno }, new int[] { 2, 1, 2 });
                createMixedOrder(ecoValle, LocalDateTime.now().minusMinutes(10), false, 1010,
                                new Cheese[] { azul, chevrett }, new int[] { 4, 2 });

                // Save updates
                userRepository.save(user1);
                userRepository.save(user2);
                userRepository.save(userAdmin);
                userRepository.save(userAdmin2);
                userRepository.save(tiendaRiaza);
                userRepository.save(supermercadoAldeonte);
                userRepository.save(queseriaAyllon);
                userRepository.save(colmadoSepulveda);
                userRepository.save(tiendaMaderuelo);
                userRepository.save(mercadoCantalejo);
                userRepository.save(distribucionesBurgomillodo);
                userRepository.save(ecoValle);
                userRepository.save(gourmetPedraza);
                userRepository.save(charcuteriaBoceguillas);
                cheeseRepository.save(semicurado);
                cheeseRepository.save(azul);
                cheeseRepository.save(curado);
                cheeseRepository.save(chevrett);
                cheeseRepository.save(tierno);

        }
}
