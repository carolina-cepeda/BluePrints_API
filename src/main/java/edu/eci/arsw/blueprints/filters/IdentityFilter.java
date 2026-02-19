package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import org.springframework.stereotype.Component;

/**
 * Default filter: returns the blueprint unchanged.
 */
@Component
@org.springframework.context.annotation.Profile("default")
public class IdentityFilter implements BlueprintsFilter {
    @Override
    public Blueprint apply(Blueprint bp) {
        return bp;
    }
}
