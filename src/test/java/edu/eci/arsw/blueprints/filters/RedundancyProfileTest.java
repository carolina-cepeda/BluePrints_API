package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("redundancy")
class RedundancyProfileTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private BlueprintsServices services;

    @Test
    void shouldLoadRedundancyFilter() {
        assertThat(context.getBean(RedundancyFilter.class)).isNotNull();
        assertThat(context.getBeansOfType(IdentityFilter.class)).isEmpty();
    }
}
