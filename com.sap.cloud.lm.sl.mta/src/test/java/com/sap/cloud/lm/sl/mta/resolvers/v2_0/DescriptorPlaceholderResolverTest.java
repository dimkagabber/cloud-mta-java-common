package com.sap.cloud.lm.sl.mta.resolvers.v2_0;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sap.cloud.lm.sl.common.util.Callable;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.common.util.TestUtil;
import com.sap.cloud.lm.sl.mta.MtaTestUtil;
import com.sap.cloud.lm.sl.mta.handlers.v2_0.ConfigurationParser;
import com.sap.cloud.lm.sl.mta.handlers.v2_0.DescriptorParser;
import com.sap.cloud.lm.sl.mta.model.SystemParameters;
import com.sap.cloud.lm.sl.mta.model.v2_0.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.v2_0.Platform;
import com.sap.cloud.lm.sl.mta.model.v2_0.Target;
import com.sap.cloud.lm.sl.mta.resolvers.ResolverBuilder;

@RunWith(value = Parameterized.class)
public class DescriptorPlaceholderResolverTest {

    protected static final String TARGET_LOCATION = "target.json";
    protected static final String SYSTEM_PARAMETERS_LOCATION = "system-parameters.json";

    protected final String deploymentDescriptorLocation;
    protected final String platformLocation;
    protected final String expected;

    protected DescriptorPlaceholderResolver resolver;

    @Parameters
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
// @formatter:off
            // (00) Placeholder in requires dependency:
            {
                "mtad-01.yaml", "platform-1.json", "R:result-01.json",
            },
            // (01) Concatenation of placeholders:
            {
                "mtad-02.yaml", "platform-1.json",  "R:result-02.json",
            },
            // (02) Placeholder in extension descriptor properties:
            {
                "mtad-03.yaml",  "platform-1.json", "R:result-03.json",
            },
            // (03) Placeholder in module:
            {
                "mtad-04.yaml",  "platform-1.json", "R:result-04.json",
            },
            // (04) Placeholder in resource:
            {
                "mtad-05.yaml",  "platform-1.json",  "R:result-05.json",
            },
            // (05) Placeholder in provided dependency:
            {
                "mtad-06.yaml",  "platform-1.json",  "R:result-06.json",
            },
            // (06) Unable to resolve placeholder in requires dependency:
            {
                "mtad-07.yaml",  "platform-1.json",  "E:Unable to resolve \"pricing#pricing-db#non-existing\"",
            },
            // (07) Unable to resolve placeholder in extension descriptor properties:
            {
                "mtad-08.yaml",  "platform-1.json",  "E:Unable to resolve \"non-existing\"",
            },
            // (08) Unable to resolve placeholder in module:
            {
                "mtad-09.yaml",  "platform-1.json",  "E:Unable to resolve \"pricing#non-existing\"",
            },
            // (09) Placeholder in untyped resource:
            {
                "mtad-10.yaml",  "platform-1.json",  "R:result-10.json",
            },
            // (10) Placeholder in module type:
            {
                "mtad-11.yaml",  "platform-2.json", "R:result-11.json",
            },
            // (11) Circular reference in module type:
            {
                "mtad-11.yaml",  "platform-3.json", "E:Circular reference detected in \"pricing#pricing-db#baz\"",
            },
            // (12) Placeholder in requires dependency (default-uri):
            {
                "mtad-13.yaml",  "platform-1.json", "R:result-13.json",
            },
            // (13) Placeholders in a map in resource parameters:
            {
                "mtad-14.yaml",  "platform-1.json",  "R:result-14.json",
            },
            // (14) Placeholders in a list in a map in resource parameters:
            {
                "mtad-15.yaml",  "platform-1.json",  "R:result-15.json",
            },
            // (15) Circular reference in a module parameter:
            {
                "mtad-16.yaml",  "platform-1.json",  "E:Circular reference detected in \"foo#bar#test_1\"",
            },
            // (16) Circular reference in a module parameter:
            {
                "mtad-17.yaml",  "platform-1.json",  "E:Circular reference detected in \"foo#test_1\"",
            },
            // (17) Global mta parameters to be resolved in lower scopes
            {
                "mtad-18.yaml",  "platform-1.json", "R:result-16.json",  
            },
// @formatter:on
        });
    }

    public DescriptorPlaceholderResolverTest(String deploymentDescriptorLocation, String platformLocation, String expected) {
        this.deploymentDescriptorLocation = deploymentDescriptorLocation;
        this.platformLocation = platformLocation;
        this.expected = expected;
    }

    @Before
    public void initialize() throws Exception {
        DescriptorParser descriptorParser = getDescriptorParser();

        DeploymentDescriptor deploymentDescriptor = getDeploymentDescriptor(descriptorParser);

        ConfigurationParser configurationParser = getConfigurationParser();

        Target target = getTarget(configurationParser);
        Platform platform = getPlatform(configurationParser);

        SystemParameters systemParameters = JsonUtil.fromJson(TestUtil.getResourceAsString(SYSTEM_PARAMETERS_LOCATION, getClass()),
            SystemParameters.class);

        resolver = getDescriptorPlaceholderResolver(deploymentDescriptor, target, platform, systemParameters);
    }

    protected DescriptorPlaceholderResolver getDescriptorPlaceholderResolver(DeploymentDescriptor deploymentDescriptor, Target target,
        Platform platform, SystemParameters systemParameters) {
        return new DescriptorPlaceholderResolver(deploymentDescriptor, platform, target, systemParameters, new ResolverBuilder(),
            new ResolverBuilder());
    }

    protected Platform getPlatform(ConfigurationParser configurationParser) {
        return (Platform) MtaTestUtil.loadPlatforms(platformLocation, configurationParser, getClass())
            .get(0);
    }

    protected Target getTarget(ConfigurationParser configurationParser) {
        return (Target) MtaTestUtil.loadTargets(TARGET_LOCATION, configurationParser, getClass())
            .get(0);
    }

    protected ConfigurationParser getConfigurationParser() {
        return new ConfigurationParser();
    }

    protected DeploymentDescriptor getDeploymentDescriptor(DescriptorParser descriptorParser) {
        return (DeploymentDescriptor) MtaTestUtil.loadDeploymentDescriptor(deploymentDescriptorLocation, descriptorParser, getClass());
    }

    protected DescriptorParser getDescriptorParser() {
        return new DescriptorParser();
    }

    @Test
    public void testResolve() {
        TestUtil.test(new Callable<DeploymentDescriptor>() {
            @Override
            public DeploymentDescriptor call() throws Exception {
                return resolver.resolve();
            }
        }, expected, getClass());
    }

}
