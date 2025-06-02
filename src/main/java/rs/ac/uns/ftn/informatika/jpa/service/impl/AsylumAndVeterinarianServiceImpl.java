package rs.ac.uns.ftn.informatika.jpa.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.model.AsylumAndVeterinarian;
import rs.ac.uns.ftn.informatika.jpa.repository.AsylumAndVeterinarianRepository;
import rs.ac.uns.ftn.informatika.jpa.service.AsylumAndVeterinarianService;

import java.util.List;

@Service
public class AsylumAndVeterinarianServiceImpl implements AsylumAndVeterinarianService {

    @Autowired
    private AsylumAndVeterinarianRepository asylumAndVeterinarianRepository;

    public AsylumAndVeterinarian save(AsylumAndVeterinarian as) {
        return this.asylumAndVeterinarianRepository.save(as);
    }

    public List<AsylumAndVeterinarian> findAll() {
        return this.asylumAndVeterinarianRepository.findAll();
    }
}
