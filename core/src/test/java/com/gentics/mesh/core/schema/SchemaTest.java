package com.gentics.mesh.core.schema;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Locale;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gentics.mesh.core.data.service.I18NUtil;
import com.gentics.mesh.core.rest.error.HttpStatusCodeErrorException;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaImpl;
import com.gentics.mesh.core.rest.schema.FieldSchemaContainer;
import com.gentics.mesh.core.rest.schema.ListFieldSchema;
import com.gentics.mesh.core.rest.schema.Microschema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.impl.HtmlFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.ListFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.SchemaImpl;
import com.gentics.mesh.json.JsonUtil;
import com.gentics.mesh.json.MeshJsonException;
import com.gentics.mesh.util.FieldUtil;

public class SchemaTest {

	private void expectErrorOnValidate(FieldSchemaContainer container, String bodyMessageI18nKey, String... i18nParams) {
		try {
			container.validate();
			fail("No exception was thrown but we would expect a {" + bodyMessageI18nKey + "} error.");
		} catch (HttpStatusCodeErrorException e) {
			assertEquals("The exception did not contain the expected message.", bodyMessageI18nKey, e.getMessage());
			assertArrayEquals(i18nParams, e.getI18nParameters());
			// Lets check english translation
			Locale en = Locale.ENGLISH;
			String text = I18NUtil.get(en, bodyMessageI18nKey, i18nParams);
			assertNotEquals("English translation for key " + bodyMessageI18nKey + " not found", text, bodyMessageI18nKey);

			// Lets check german translation
			Locale de = Locale.GERMAN;
			text = I18NUtil.get(de, bodyMessageI18nKey, i18nParams);
			assertNotEquals("German translation for key " + bodyMessageI18nKey + " not found", text, bodyMessageI18nKey);
		}
	}

	@Test
	public void testSimpleSchema() throws IOException {
		Schema schema = new SchemaImpl();
		schema.setName("dummySchema");
		schema.setContainer(true);
		schema.addField(new HtmlFieldSchemaImpl().setLabel("Label").setName("Name").setRequired(true));
		validateSchema(schema);
	}

	@Test
	public void testComplexSchema() throws IOException {
		Schema schema = new SchemaImpl();
		schema.setName("dummySchema");
		schema.setDisplayField("name");
		schema.setSegmentField("name");
		schema.setContainer(true);
		schema.addField(FieldUtil.createStringFieldSchema("name"));
		schema.addField(FieldUtil.createHtmlFieldSchema("name_1").setLabel("label_1").setRequired(true));
		schema.addField(FieldUtil.createStringFieldSchema("name_2").setLabel("label_2").setRequired(true));
		schema.addField(FieldUtil.createNumberFieldSchema("name_3").setLabel("label_3").setRequired(true));
		schema.addField(FieldUtil.createDateFieldSchema("name_4").setLabel("label_4").setRequired(true));
		schema.addField(FieldUtil.createBooleanFieldSchema("name_5").setLabel("label_5").setRequired(true));

		ListFieldSchema listFieldSchema = new ListFieldSchemaImpl();
		listFieldSchema.setLabel("label_7").setName("name_7").setRequired(true);
		listFieldSchema.setAllowedSchemas(new String[] { "folder", "videos" });
		listFieldSchema.setListType("node");
		listFieldSchema.setMax(10);
		listFieldSchema.setMin(3);
		schema.addField(listFieldSchema);

		// MicroschemaFieldSchema microschemaFieldSchema = new MicroschemaFieldSchemaImpl();
		// microschemaFieldSchema.setLabel("label_8").setName("name_8").setRequired(true);
		// microschemaFieldSchema.setAllowedMicroSchemas(new String[] { "content", "folder" });
		//
		// StringFieldSchema stringFieldSchema = new StringFieldSchemaImpl();
		// stringFieldSchema.setName("field1").setLabel("label1");
		// microschemaFieldSchema.getFields().add(stringFieldSchema);
		// schema.addField(microschemaFieldSchema);

		schema.validate();
		validateSchema(schema);
	}

	private void validateSchema(Schema schema) throws JsonParseException, JsonMappingException, IOException {
		assertNotNull(schema);
		String json = JsonUtil.toJson(schema);
		System.out.println(json);
		assertNotNull(json);
		Schema deserializedSchema = JsonUtil.readSchema(json, SchemaImpl.class);
		assertEquals(schema.getFields().size(), deserializedSchema.getFields().size());
		assertNotNull(deserializedSchema);
	}

	@Test
	public void testNoNameInvalid() throws MeshJsonException {
		Schema schema = new SchemaImpl();
		expectErrorOnValidate(schema, "schema_error_no_name");
	}

	@Test
	public void testNoFieldsInvalid() throws MeshJsonException {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		expectErrorOnValidate(schema, "schema_error_no_fields");
	}

	@Test
	public void testSegmentFieldNotSet() throws MeshJsonException {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		schema.setDisplayField("name");
		schema.addField(FieldUtil.createStringFieldSchema("name"));
		expectErrorOnValidate(schema, "schema_error_segmentfield_not_set");
	}

	@Test
	public void testSegmentFieldInvalid() throws MeshJsonException {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		schema.setSegmentField("invalid");
		schema.setDisplayField("name");
		schema.addField(FieldUtil.createStringFieldSchema("name"));
		expectErrorOnValidate(schema, "schema_error_segmentfield_invalid", "invalid");
	}

	@Test
	public void testMinimalSchemaValid() throws MeshJsonException {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		schema.setSegmentField("name");
		schema.setDisplayField("name");
		schema.addField(FieldUtil.createStringFieldSchema("name"));
		schema.validate();
	}

	@Test
	public void testDisplayFieldNotSet() throws MeshJsonException {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		schema.setSegmentField("name");
		schema.addField(FieldUtil.createStringFieldSchema("name"));
		expectErrorOnValidate(schema, "schema_error_displayfield_not_set");
	}

	@Test
	public void testDisplayFieldInvalid() {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		schema.setSegmentField("name");
		schema.setDisplayField("invalid");
		schema.addField(FieldUtil.createStringFieldSchema("name"));
		expectErrorOnValidate(schema, "schema_error_displayfield_invalid", "invalid");
	}

	@Test
	public void testDuplicateFieldSchemaName() {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		schema.setSegmentField("name");
		schema.setDisplayField("name");
		schema.addField(FieldUtil.createStringFieldSchema("name"));
		schema.addField(FieldUtil.createStringFieldSchema("name"));
		expectErrorOnValidate(schema, "schema_error_duplicate_field_name");
	}

	@Test
	public void testDuplicateFieldSchemaLabel() {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		schema.setSegmentField("name");
		schema.setDisplayField("name");
		schema.addField(FieldUtil.createStringFieldSchema("name").setLabel("conflict"));
		schema.addField(FieldUtil.createStringFieldSchema("name2").setLabel("conflict"));
		expectErrorOnValidate(schema, "schema_error_duplicate_field_label");
	}

	/**
	 * The display field must always point to a string field.
	 */
	@Test
	public void testDisplayFieldToNoStringFieldInvalid() {
		Schema schema = new SchemaImpl();
		schema.setName("test");
		schema.setSegmentField("name");
		schema.setDisplayField("name");
		schema.addField(FieldUtil.createBinaryFieldSchema("name"));
		expectErrorOnValidate(schema, "schema_error_displayfield_type_invalid", "name");
	}

	/**
	 * The segment field must always point to a string or binary field.
	 */
	@Test
	public void testSegmentFieldToNoStringOrBinaryFieldInvalid() {

	}

	@Test
	public void testMicroschemaUnsupportedFieldTypeBinary() {
		Microschema schema = new MicroschemaImpl();
		schema.setName("test");
		schema.setDescription("some blub");
		schema.addField(FieldUtil.createBinaryFieldSchema("binary"));
		expectErrorOnValidate(schema, "microschema_error_field_type_not_allowed", "binary", "binary");
	}

	@Test
	public void testMicroschemaUnsupportedFieldTypeMicronode() {
		Microschema schema = new MicroschemaImpl();
		schema.setName("test");
		schema.setDescription("some blub");
		schema.addField(FieldUtil.createMicronodeFieldSchema("micronode"));
		expectErrorOnValidate(schema, "microschema_error_field_type_not_allowed", "micronode", "micronode");
	}

	@Test
	public void testMicroschemaUnsupportedFieldTypeMicronodeList() {
		Microschema schema = new MicroschemaImpl();
		schema.setName("test");
		schema.setDescription("some blub");
		schema.addField(FieldUtil.createListFieldSchema("list").setListType("micronode"));
		expectErrorOnValidate(schema, "microschema_error_field_type_not_allowed", "list", "list:micronode");
	}

	@Test
	public void testMicroschemaUnsupportedFieldTypeBinaryList() {
		Microschema schema = new MicroschemaImpl();
		schema.setName("test");
		schema.setDescription("some blub");
		schema.addField(FieldUtil.createListFieldSchema("list").setListType("binary"));
		expectErrorOnValidate(schema, "microschema_error_field_type_not_allowed", "list", "list:binary");
	}

}
