/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller;

import org.springframework.samples.petclinic.rest.controller.v1.VisitRestControllerV1;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link VisitRestControllerV1}
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@ContextConfiguration(classes=ApplicationTestConfig.class)
@WebAppConfiguration
class VisitRestControllerV1Tests {

    @Autowired
    private VisitRestControllerV1 visitRestControllerV1;

    @MockitoBean
    private ClinicService clinicService;

    @Autowired
    private VisitMapper visitMapper;

    private MockMvc mockMvc;

    private List<Visit> visits;

    @BeforeEach
    void initVisits(){
        this.mockMvc = MockMvcBuilders.standaloneSetup(visitRestControllerV1)
        			.setControllerAdvice(new ExceptionControllerAdvice())
        			.build();

        visits = new ArrayList<>();

    	Owner owner = new Owner();
    	owner.setId(1);
    	owner.setFirstName("Eduardo");
    	owner.setLastName("Rodriquez");
    	owner.setAddress("2693 Commerce St.");
    	owner.setCity("McFarland");
    	owner.setTelephone("6085558763");

    	PetType petType = new PetType();
    	petType.setId(2);
    	petType.setName("dog");

    	Pet pet = new Pet();
    	pet.setId(8);
    	pet.setName("Rosy");
        pet.setBirthDate(LocalDate.now());
    	pet.setOwner(owner);
    	pet.setType(petType);

        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

    	Visit visit = new Visit();
    	visit.setId(2);
    	visit.setPet(pet);
        visit.setDate(LocalDate.now());
    	visit.setDescription("rabies shot");
        visit.setVeterinarian(vet);
    	visits.add(visit);

    	visit = new Visit();
    	visit.setId(3);
    	visit.setPet(pet);
        visit.setDate(LocalDate.now());
    	visit.setDescription("neutered");
        visit.setVeterinarian(null);
    	visits.add(visit);


    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetVisitSuccess() throws Exception {
    	given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
        this.mockMvc.perform(get("/api/visits/2")
        	.accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.description").value("rabies shot"));
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetVisitNotFound() throws Exception {
        given(this.clinicService.findVisitById(999)).willReturn(null);
        this.mockMvc.perform(get("/api/visits/999")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetAllVisitsSuccess() throws Exception {
    	given(this.clinicService.findAllVisits()).willReturn(visits);
        this.mockMvc.perform(get("/api/visits")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
        	.andExpect(jsonPath("$.[0].id").value(2))
        	.andExpect(jsonPath("$.[0].description").value("rabies shot"))
        	.andExpect(jsonPath("$.[1].id").value(3))
        	.andExpect(jsonPath("$.[1].description").value("neutered"));
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetAllVisitsNotFound() throws Exception {
    	visits.clear();
    	given(this.clinicService.findAllVisits()).willReturn(visits);
        this.mockMvc.perform(get("/api/visits")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testCreateVisitSuccess() throws Exception {
    	Visit newVisit = visits.get(0);
    	newVisit.setId(999);
    	Vet vet = new Vet();
    	vet.setId(1);
    	vet.setFirstName("James");
    	vet.setLastName("Carter");
    	newVisit.setVeterinarian(vet);
    	given(this.clinicService.findVetById(1)).willReturn(vet);
    	org.mockito.Mockito.doAnswer(invocation -> null).when(this.clinicService).saveVisit(org.mockito.ArgumentMatchers.any());
    	ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
     	this.mockMvc.perform(post("/api/visits")
     		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
     		.andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testCreateVisitError() throws Exception {
    	Visit newVisit = visits.get(0);
    	newVisit.setId(null);
        newVisit.setDescription(null);
    	ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
    	this.mockMvc.perform(post("/api/visits")
        		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testUpdateVisitSuccess() throws Exception {
    	given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
    	VisitFieldsDto visitFieldsDto = new VisitFieldsDto();
    	visitFieldsDto.setDate(LocalDate.now());
    	visitFieldsDto.setDescription("rabies shot test");
    	visitFieldsDto.setVeterinarianId(1);
    	given(this.clinicService.findVetById(1)).willReturn(new Vet());
    	ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitFieldsDto);
     	this.mockMvc.perform(put("/api/visits/2")
     		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
         	.andExpect(status().isNoContent());

     	this.mockMvc.perform(get("/api/visits/2")
            	.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
             .andExpect(status().isOk())
             .andExpect(content().contentType("application/json"))
             .andExpect(jsonPath("$.id").value(2))
             .andExpect(jsonPath("$.description").value("rabies shot test"));
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testUpdateVisitError() throws Exception {
    	VisitFieldsDto visitFieldsDto = new VisitFieldsDto();
    	visitFieldsDto.setDate(LocalDate.now());
    	visitFieldsDto.setDescription(null);
    	visitFieldsDto.setVeterinarianId(1);
    	ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitFieldsDto);
     	this.mockMvc.perform(put("/api/visits/2")
     		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
         	.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testDeleteVisitSuccess() throws Exception {
    	Visit newVisit = visits.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
    	given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
    	this.mockMvc.perform(delete("/api/visits/2")
    		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testDeleteVisitError() throws Exception {
    	Visit newVisit = visits.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
        given(this.clinicService.findVisitById(999)).willReturn(null);
        this.mockMvc.perform(delete("/api/visits/999")
    		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testCreateVisitSuccessWithVeterinarian() throws Exception {
    	Visit newVisit = new Visit();
    	newVisit.setId(null);
    	newVisit.setPet(visits.get(0).getPet());
    	newVisit.setDate(LocalDate.now());
    	newVisit.setDescription("checkup");
    	Vet vet = new Vet();
    	vet.setId(1);
    	vet.setFirstName("James");
    	vet.setLastName("Carter");
    	newVisit.setVeterinarian(vet);
    	given(this.clinicService.findVetById(1)).willReturn(vet);
    	org.mockito.Mockito.doAnswer(invocation -> null).when(this.clinicService).saveVisit(org.mockito.ArgumentMatchers.any());
    	ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
     	this.mockMvc.perform(post("/api/visits")
     		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
     		.andExpect(status().isCreated())
            .andExpect(jsonPath("$.veterinarianId").value(1));
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testCreateVisitWithInvalidVeterinarian() throws Exception {
    	Visit newVisit = new Visit();
    	newVisit.setId(null);
    	newVisit.setPet(visits.get(0).getPet());
    	newVisit.setDate(LocalDate.now());
    	newVisit.setDescription("checkup");
    	Vet vet = new Vet();
    	vet.setId(999);
    	vet.setFirstName("Unknown");
    	vet.setLastName("Vet");
    	newVisit.setVeterinarian(vet);
    	given(this.clinicService.findVetById(999)).willReturn(null);
    	org.mockito.Mockito.doAnswer(invocation -> null).when(this.clinicService).saveVisit(org.mockito.ArgumentMatchers.any());
    	ObjectMapper mapper = new ObjectMapper();
         String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisitDto(newVisit));
     	this.mockMvc.perform(post("/api/visits")
     		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
     		.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testUpdateVisitSetVeterinarianToNull() throws Exception {
    	given(this.clinicService.findVisitById(2)).willReturn(visits.get(0));
    	// Create VisitFieldsDto with veterinarianId = null (clear veterinarian)
    	VisitFieldsDto visitFieldsDto = new VisitFieldsDto();
    	visitFieldsDto.setDate(LocalDate.now());
    	visitFieldsDto.setDescription("updated description");
    	visitFieldsDto.setVeterinarianId(null);
    	ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitFieldsDto);
     	this.mockMvc.perform(put("/api/visits/2")
     		.content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
         	.andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testListVisitsIncludesVeterinarian() throws Exception {
    	Vet vet = new Vet();
    	vet.setId(1);
    	vet.setFirstName("James");
    	vet.setLastName("Carter");
    	visits.get(0).setVeterinarian(vet);
    	given(this.clinicService.findAllVisits()).willReturn(visits);
        this.mockMvc.perform(get("/api/visits")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].veterinarian.id").value(1))
            .andExpect(jsonPath("$.[0].veterinarian.firstName").value("James"))
            .andExpect(jsonPath("$.[1].veterinarian").doesNotExist());
    }

}
