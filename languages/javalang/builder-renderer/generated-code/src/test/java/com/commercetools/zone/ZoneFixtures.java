package com.commercetools.zone;

import com.commercetools.client.ApiRoot;
import com.commercetools.models.Zone.Zone;
import com.commercetools.models.Zone.ZoneDraft;
import com.commercetools.models.Zone.ZoneDraftBuilder;
import com.commercetools.utils.CommercetoolsTestUtils;
import org.junit.Assert;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ZoneFixtures {
    
    public static void withZone(final Consumer<Zone> consumer) {
        Zone zone = createZone();
        consumer.accept(zone);
        deleteZone(zone.getId(), zone.getVersion());
    }
    
    public static void withUpdateableZone(final UnaryOperator<Zone> operator) {
        Zone zone = createZone();
        zone = operator.apply(zone);
        deleteZone(zone.getId(), zone.getVersion());
    }
    
    public static Zone createZone() {
        ZoneDraft zoneDraft = ZoneDraftBuilder.of()
                .name(CommercetoolsTestUtils.randomString())
                .key(CommercetoolsTestUtils.randomKey())
                .build();
        
        Zone zone = ApiRoot.withProjectKeyValue(CommercetoolsTestUtils.getProjectKey())
                .zones()
                .post(zoneDraft)
                .executeBlocking();

        Assert.assertNotNull(zone);
        Assert.assertEquals(zoneDraft.getKey(), zone.getKey());
        
        return zone;
    }
    
    public static Zone deleteZone(final String id, final Long version) {
        Zone zone = ApiRoot.withProjectKeyValue(CommercetoolsTestUtils.getProjectKey())
                .zones()
                .withId(id)
                .delete()
                .addVersion(version)
                .executeBlocking();

        Assert.assertNotNull(zone);
        Assert.assertEquals(zone.getId(), id);
        
        return zone;
    }
    
}
