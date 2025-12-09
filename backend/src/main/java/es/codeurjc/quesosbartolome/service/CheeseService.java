package es.codeurjc.quesosbartolome.service;

import es.codeurjc.quesosbartolome.dto.CheeseDTO;
import es.codeurjc.quesosbartolome.dto.CheeseMapper;
import es.codeurjc.quesosbartolome.model.Cheese;
import es.codeurjc.quesosbartolome.repository.CheeseRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
} 


