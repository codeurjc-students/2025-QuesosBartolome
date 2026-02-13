package es.codeurjc.quesosbartolome.service;

import es.codeurjc.quesosbartolome.dto.CheeseDTO;
import es.codeurjc.quesosbartolome.dto.CheeseMapper;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Blob;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CheeseService {

    @Autowired
    private CheeseRepository cheeseRepository;

    @Autowired
    private CheeseMapper cheeseMapper;

    public List<CheeseDTO> findAll() {
        return cheeseRepository.findAll()
                .stream()
                .map(cheeseMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CheeseDTO> findById(Long id) {
        return cheeseRepository.findById(id)
                .map(cheeseMapper::toDTO);
    }

    public Optional<Blob> getCheeseImageById(Long id) {
        Optional<Cheese> cheeseOpt = cheeseRepository.findById(id);

        if (cheeseOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(cheeseOpt.get().getImage());
    }

    public CheeseDTO createCheese(CheeseDTO dto) {

        if (cheeseRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Cheese with that name already exists");
        }

        if (dto.price() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        Cheese cheese = cheeseMapper.toDomain(dto);

        // JPA @ElementCollection requires a mutable list
        if (cheese.getBoxes() == null || cheese.getBoxes().isEmpty()) {
            cheese.setBoxes(new java.util.ArrayList<>());
        }

        // Set default image
        try {
            InputStream defaultImageStream = getClass().getResourceAsStream("/images/queso-default.jpg");
            if (defaultImageStream != null) {
                byte[] defaultBytes = defaultImageStream.readAllBytes();
                Blob defaultBlob = BlobProxy.generateProxy(defaultBytes);
                cheese.setImage(defaultBlob);
            } else {
                cheese.setImage(null);
            }
        } catch (Exception e) {
            cheese.setImage(null);
        }

        Cheese saved = cheeseRepository.save(cheese);
        return cheeseMapper.toDTO(saved);
    }

    public CheeseDTO updateCheese(Long id, CheeseDTO dto) {
        Optional<Cheese> cheeseOpt = cheeseRepository.findById(id);

        if (cheeseOpt.isEmpty()) {
            throw new IllegalArgumentException("Cheese not found");
        }

        Cheese cheese = cheeseOpt.get();

        // Check if name is being changed and if it already exists
        if (!cheese.getName().equals(dto.name()) && cheeseRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Cheese with that name already exists");
        }

        if (dto.price() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        // Update fields
        cheese.setName(dto.name());
        cheese.setPrice(dto.price());
        cheese.setDescription(dto.description());
        cheese.setManufactureDate(dto.manufactureDate());
        cheese.setExpirationDate(dto.expirationDate());
        cheese.setType(dto.type());

        Cheese updated = cheeseRepository.save(cheese);
        return cheeseMapper.toDTO(updated);
    }

    public boolean saveCheeseImage(Long id, MultipartFile file) throws Exception {

        Optional<Cheese> cheeseOpt = cheeseRepository.findById(id);

        if (cheeseOpt.isEmpty()) {
            return false;
        }

        Cheese cheese = cheeseOpt.get();

        if (file == null || file.isEmpty()) {

            InputStream defaultImageStream = getClass().getResourceAsStream("/images/queso-default.jpg");

            if (defaultImageStream == null) {
                throw new RuntimeException("The file queso-default.jpg was not found in the resources folder.");
            }

            byte[] defaultBytes = defaultImageStream.readAllBytes();

            Blob defaultBlob = BlobProxy.generateProxy(defaultBytes);

            cheese.setImage(defaultBlob);

        } else {
            Blob blob = BlobProxy.generateProxy(file.getBytes());
            cheese.setImage(blob);
        }

        cheeseRepository.save(cheese);

        return true;
    }

    public boolean deleteCheese(Long id) {
        Optional<Cheese> cheeseOpt = cheeseRepository.findById(id);

        if (cheeseOpt.isEmpty()) {
            return false;
        }

        cheeseRepository.deleteById(id);
        return true;
    }

    public CheeseDTO addBox(Long id, Double weight) {
        Optional<Cheese> cheeseOpt = cheeseRepository.findById(id);

        if (cheeseOpt.isEmpty()) {
            throw new IllegalArgumentException("Cheese not found");
        }

        Cheese cheese = cheeseOpt.get();

        if (cheese.getBoxes() == null) {
            cheese.setBoxes(new java.util.ArrayList<>());
        }

        cheese.getBoxes().add(weight);

        Cheese updated = cheeseRepository.save(cheese);
        return cheeseMapper.toDTO(updated);
    }

    public CheeseDTO removeBox(Long id, int boxIndex) {
        Optional<Cheese> cheeseOpt = cheeseRepository.findById(id);

        if (cheeseOpt.isEmpty()) {
            throw new IllegalArgumentException("Cheese not found");
        }

        Cheese cheese = cheeseOpt.get();

        if (cheese.getBoxes() == null || cheese.getBoxes().isEmpty()) {
            throw new IllegalArgumentException("No boxes to remove");
        }

        if (boxIndex < 0 || boxIndex >= cheese.getBoxes().size()) {
            throw new IllegalArgumentException("Invalid box index");
        }

        cheese.getBoxes().remove(boxIndex);

        Cheese updated = cheeseRepository.save(cheese);
        return cheeseMapper.toDTO(updated);
    }

}
