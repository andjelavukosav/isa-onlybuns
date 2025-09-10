package rs.ac.uns.ftn.informatika.jpa.service;


import rs.ac.uns.ftn.informatika.jpa.model.AsylumAndVeterinarian;

import java.util.List;

public interface AsylumAndVeterinarianService {
    AsylumAndVeterinarian save(AsylumAndVeterinarian asylumAndVeterinarian);
    List<AsylumAndVeterinarian> findAll ();

}
