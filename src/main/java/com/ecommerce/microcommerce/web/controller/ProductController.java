package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.model.User;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@Api(value = "MicroCommerce", description="API pour des opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    //Récupérer la liste des produits
    @ApiOperation(value = "get list of product", response = Iterable.class, tags = "listeProduits")
    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Product> ps = productDao.findAll();

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(ps);

        return getMappingJacksonValue(user, produitsFiltres);

    }

    private MappingJacksonValue getMappingJacksonValue(User user, MappingJacksonValue produitsFiltres) {
        if(user.getRole().equals("user")){
            FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", SimpleBeanPropertyFilter.serializeAllExcept("prixAchat","prix"));
            produitsFiltres.setFilters(listDeNosFiltres);
            return produitsFiltres;
        }else {
            FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", SimpleBeanPropertyFilter.serializeAllExcept("prixAchat"));
            produitsFiltres.setFilters(listDeNosFiltres);
            return produitsFiltres;
        }
    }

    @Secured("admin")
    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!", tags = "afficherUnProduit")
    @GetMapping(value = "/Produits/{id}")
    public MappingJacksonValue afficherUnProduit(@PathVariable int id) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Product produit = productDao.findById(id);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produit);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return getMappingJacksonValue(user, produitsFiltres);


    }




    //ajouter un produit
    @ApiOperation(value = "Create a product", tags = "ajouterProduit")
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {

        Product productAdded =  productDao.save(product);

        if (productAdded == null )
            return ResponseEntity.noContent().build();
        else if(product.getPrix()==0)
            throw new ProduitGratuitException("L'ajout d'un produit avec un prix de 0 est IMPOSSIBLE.");

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
    @ApiOperation(value = "delete a product", tags = "supprimerProduit")
    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.delete(id);
    }
    @ApiOperation(value = "update a product", tags = "updateProduit")
    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }


    //Pour les tests
    @ApiOperation(value = "test a function by trying to find a product by price", response = Iterable.class, tags = "testeDeRequetes")
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(400);
    }
    //calcule de la marge de chaque produit

    @ApiOperation(value = "calculer la marge", tags = "calculerMargeProduit")
    @GetMapping(value = "/AdminProduits")
    public List<String> calculerMargeProduit(){
        List<Product> ps = productDao.findAll();
        List<String> psm= new ArrayList<>();
        for (Product p:ps) {
            int m=p.getPrix()-p.getPrixAchat();
            psm.add(p.toString()+":"+m+"");
        }
        return psm;
    }

    @GetMapping(value = "/user")
    public ResponseEntity<User> getUser(){

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return  new ResponseEntity<User>(user, HttpStatus.OK);
    }



}
