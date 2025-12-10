package es.codeurjc.quesosbartolome.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.model.User;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service 
public class DataBaseInitializer {

    @Autowired
    private CheeseRepository cheeseRepository;

    @Autowired
    private UserRepository userRepository;

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


    @PostConstruct
    public void init() throws IOException, URISyntaxException {
        // Create Cheese 1
        Cheese semicurado = new Cheese();
        semicurado.setName("Semicurado");
        semicurado.setPrice(17.50);
        semicurado.setDescription("Queso de pasta prensada madurado durante 21 días, con un sabor que comienza suave y cremoso pero se intensifica al final. Aromático sin resultar demasiado fuerte, ofrece un equilibrio agradable que lo hace fácil de disfrutar solo o acompañado.");
        semicurado.setManufactureDate(Date.valueOf(LocalDate.now().minusMonths(2)));
        semicurado.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(10)));
        semicurado.setType("Hard");
        semicurado.setBoxes(List.of(6.32, 5.87, 5.82, 6.56, 5.98, 6.34, 6.41, 6.03, 5.79, 6.22));
        semicurado.setImage(saveImage("images/queso-default.jpg"));
        


        // Create Cheese 2
        Cheese azul = new Cheese();
        azul.setName("Azul");
        azul.setPrice(15.00);
        azul.setDescription("Queso azul de tradición clásica, de pasta prensada y curación cuidada, con vetas de moho que aportan un sabor intenso y profundo. Su textura es cremosa y su carácter es marcado sin llegar a ser excesivamente agresivo, ofreciendo ese equilibrio típico del azul de toda la vida.");
        azul.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(3)));
        azul.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(2)));
        azul.setType("Soft");
        azul.setBoxes(List.of(4.32, 4.87, 5.12, 4.56, 4.98, 5.34, 4.41, 5.03, 4.79, 5.22));
        azul.setImage(saveImage("images/queso-default.jpg"));
        
        
        // Create Cheese 3
        Cheese Curado = new Cheese();
        Curado.setName("Curado"); 
        Curado.setPrice(17.50); 
        Curado.setDescription("Queso de pasta prensada madurado durante mes y medio, con una textura más firme y un sabor más desarrollado. Mantiene un carácter equilibrado: comienza suave pero gana intensidad sin llegar a ser excesivamente fuerte.");
        Curado.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(5))); 
        Curado.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(3)));
        Curado.setType("Semi-Hard");
        Curado.setBoxes(List.of(6.32, 5.87, 5.82, 6.56, 5.98, 6.34, 6.41, 6.03, 5.79, 6.22));
        Curado.setImage(saveImage("images/queso-default.jpg"));
        

        // Create Cheese 4
        Cheese Chevrett = new Cheese();
        Chevrett.setName("Chevrett"); 
        Chevrett.setPrice(20.00); 
        Chevrett.setDescription("Queso de pasta blanda y textura cremosa, similar a un camembert pero con matices propios. Su curación suave resalta un sabor delicado que se vuelve más aromático con el tiempo, sin resultar fuerte, ideal para quienes disfrutan de quesos tiernos pero con personalidad.");
        Chevrett.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(2)));
        Chevrett.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(1).plusWeeks(1)));
        Chevrett.setType("Goat"); 
        Chevrett.setBoxes(List.of());
        Chevrett.setImage(saveImage("images/queso-default.jpg"));
        


        // Create Cheese 5
        Cheese Tierno = new Cheese();
        Tierno.setName("Tierno"); 
        Tierno.setPrice(18.00); 
        Tierno.setDescription("Queso de pasta prensada madurado durante una semana, de textura muy suave y húmeda. Presenta un sabor ligero y lácteo, nada fuerte, ideal para quienes prefieren quesos delicados y fáciles de comer");
        Tierno.setManufactureDate(Date.valueOf(LocalDate.now().minusWeeks(2)));
        Tierno.setExpirationDate(Date.valueOf(LocalDate.now().plusMonths(1).plusWeeks(1)));
        Tierno.setType("Goat"); 
        Tierno.setBoxes(List.of(6.32, 5.87, 5.82, 6.56, 5.98, 6.34, 6.41, 6.03, 5.79, 6.22));
        Tierno.setImage(saveImage("images/queso-default.jpg"));
        
        
        
        // Create user 1
        User user1 = new User();
        user1.setName("Victor");
        user1.setPassword(passwordEncoder.encode("password123"));
        user1.setGmail("victor@example.com");
        user1.setDirection("123 Main St");
        user1.setNif("12345678A");
        user1.setRols("USER");
        user1.setImage(saveImage("images/default-profile.jpg"));

        // Create Admin 1
        User userAdmin = new User();
        userAdmin.setName("German");
        userAdmin.setPassword(passwordEncoder.encode("password123"));
        userAdmin.setGmail("german@example.com");
        userAdmin.setDirection("123 Main St");
        userAdmin.setNif("12345678A");
        userAdmin.setRols("ADMIN");
        userAdmin.setImage(saveImage("images/default-profile.jpg"));

        // Save cheeses to DB
        cheeseRepository.save(semicurado);
        cheeseRepository.save(azul);
        cheeseRepository.save(Curado);
        cheeseRepository.save(Chevrett);
        cheeseRepository.save(Tierno);

        // Save users in DB
        userRepository.save(user1);
        userRepository.save(userAdmin);
        
    }
}
