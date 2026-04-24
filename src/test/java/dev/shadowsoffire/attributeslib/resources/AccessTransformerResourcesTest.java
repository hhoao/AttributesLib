package dev.shadowsoffire.attributeslib.resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class AccessTransformerResourcesTest {
    @Test
    public void rangedAttributeBoundsAreAccessibleInProductionAndDevRuntime()
            throws IOException {
        String content =
                new String(
                        Files.readAllBytes(
                                Paths.get(
                                        System.getProperty("user.dir"),
                                        "src/main/resources/META-INF/attributeslib_at.cfg")),
                        StandardCharsets.UTF_8);

        Assert.assertTrue(
                content,
                content.contains(
                        "public net.minecraft.entity.ai.attributes.RangedAttribute field_111120_a"));
        Assert.assertTrue(
                content,
                content.contains(
                        "public net.minecraft.entity.ai.attributes.RangedAttribute field_111118_b"));
        Assert.assertTrue(
                content,
                content.contains(
                        "public net.minecraft.entity.ai.attributes.RangedAttribute minimumValue"));
        Assert.assertTrue(
                content,
                content.contains(
                        "public net.minecraft.entity.ai.attributes.RangedAttribute maximumValue"));
    }
}
