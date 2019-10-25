package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JsonSchemaInferrer.toStringList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSchemaInferrerTest {

  private final ObjectMapper mapper = new ObjectMapper();

  private JsonNode loadJson(String fileName) {
    try (InputStream in = this.getClass().getResourceAsStream(fileName)) {
      return mapper.readTree(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  public void testSimple() throws Exception {
    final JsonNode simple = loadJson("simple.json");
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().infer(simple);
      assertTrue(schema.hasNonNull("$schema"));
      assertTrue(schema.path("$schema").textValue().contains("-04"));
      assertTrue(schema.hasNonNull("type"));
    }
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().draft06().build().infer(simple);
      assertTrue(schema.hasNonNull("$schema"));
      assertTrue(schema.path("$schema").textValue().contains("-06"));
      assertTrue(schema.hasNonNull("type"));
    }
    {
      final ObjectNode schema =
          JsonSchemaInferrer.newBuilder().draft06().outputDollarSchema(false).build().infer(simple);
      assertFalse(schema.hasNonNull("$schema"));
      assertTrue(schema.hasNonNull("type"));
    }
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().infer(simple);
      assertTrue(schema.hasNonNull("properties"));
      assertTrue(schema.path("properties").isObject());
      assertEquals("integer", schema.path("properties").path("id").path("type").textValue());
      assertEquals("string", schema.path("properties").path("slug").path("type").textValue());
      assertEquals("boolean", schema.path("properties").path("admin").path("type").textValue());
      assertEquals("null", schema.path("properties").path("avatar").path("type").textValue());
      assertEquals("string", schema.path("properties").path("date").path("type").textValue());
      assertEquals("date-time", schema.path("properties").path("date").path("format").textValue());
      assertEquals("object", schema.path("properties").path("article").path("type").textValue());
      assertTrue(schema.path("properties").path("article").isObject());
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("title").path("type").textValue());
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("description").path("type").textValue());
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("body").path("type").textValue());
      assertEquals("array", schema.path("properties").path("comments").path("type").textValue());
      assertTrue(schema.path("properties").path("comments").path("items").isObject());
      assertEquals("string", schema.path("properties").path("comments").path("items")
          .path("properties").path("body").path("type").path(0).textValue());
      assertEquals("null", schema.path("properties").path("comments").path("items")
          .path("properties").path("body").path("type").path(1).textValue());
    }
  }

  @Test
  public void testAdvanced() throws Exception {
    final JsonNode advanced = loadJson("advanced.json");
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().infer(advanced);
      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
      assertTrue(schema.path("items").isObject());
      assertTrue(schema.path("items").path("required").isArray());
      assertEquals(Arrays.asList("id", "name", "price", "dimensions", "warehouseLocation"),
          toStringList(schema.path("items").path("required")));
      assertTrue(schema.path("items").path("properties").path("tags").isObject());
      assertEquals("integer",
          schema.path("items").path("properties").path("id").path("type").textValue());
      assertEquals("number",
          schema.path("items").path("properties").path("price").path("type").textValue());
      assertEquals(Arrays.asList("integer", "number"), toStringList(schema.path("items")
          .path("properties").path("dimensions").path("properties").path("length").path("type")));
    }
  }

}