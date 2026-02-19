package edu.eci.arsw.blueprints;

import edu.eci.arsw.blueprints.controllers.BlueprintsAPIController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BlueprintsSmokeTest {

    @Autowired
    private BlueprintsAPIController controller;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @Test
    void shouldReturnAllBlueprints() throws Exception {
        mockMvc.perform(get("/blueprints"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Blueprints retrieved successfully")));
    }

    @Test
    void shouldCreateAndRetrieveBlueprint() throws Exception {
        long timestamp = System.currentTimeMillis();
        String author = "user_" + timestamp;
        String name = "bp_" + timestamp;
        String jsonBlueprint = String.format("{\"author\":\"%s\",\"name\":\"%s\",\"points\":[{\"x\":10,\"y\":10}]}",
                author, name);

        mockMvc.perform(post("/blueprints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBlueprint))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/blueprints/" + author + "/" + name))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(author)))
                .andExpect(content().string(containsString(name)));
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/blueprints/no_existe/tampoco"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Blueprint not found")));
    }
}
