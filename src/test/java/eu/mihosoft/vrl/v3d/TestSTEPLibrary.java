package eu.mihosoft.vrl.v3d;

import static org.junit.Assert.*;

import java.io.PrintWriter;

import org.junit.Test;

import jsdai.dictionary.EAttribute;
import jsdai.dictionary.EDefined_type;
import jsdai.dictionary.EEntity_definition;
import jsdai.dictionary.ESchema_definition;
import jsdai.lang.*;

public class TestSTEPLibrary {

	@Test
	public void test() throws Exception {
		// redirect the J-SDAI system log to System.out
		SdaiSession.setLogWriter(new PrintWriter(System.out, true));

		// first open a session
		SdaiSession session = SdaiSession.openSession();

		// start a read/write transaction to allow creation of a repository
		// and a model within it
		SdaiTransaction transaction = session.startTransactionReadWriteAccess();

		SdaiRepository repo = session.createRepository("", null);
		repo.openRepository();
		A_string descriptions = repo.getDescription();
		descriptions.addByIndex(1, "Example program to generate a very basic AP214 p21 file");
		A_string authors = repo.getAuthor();
		authors.addByIndex(1, "Alfonsas Stonis");
		authors.addByIndex(2, "Gintaras Palubeckis");
		A_string organizations = repo.getOrganization();
		organizations.addByIndex(1, "LKSoftWare GmbH");
		repo.setOriginatingSystem(
				session.getSdaiImplementation().getName() + " " + session.getSdaiImplementation().getLevel());
		repo.setAuthorization("Lothar Klein");

		// Search for the SdaiModel, containing the dictionary data of AP214.
		// Do not forget to include a line in jsdai.properties file, which informs
		// JSDAI that automotive design data dictionary model shall be created.
		// The line may look like this:
		// jsdai.SAutomotive_design=AUTOMOTIVE_DESIGN_CC1;AUTOMOTIVE_DESIGN_CC2
		SdaiModel dictionaryModel = null;
		String schemaName = "AUTOMOTIVE_DESIGN_DICTIONARY_DATA";
		SchemaInstance dictionaryData = session.getDataDictionary();
		ASdaiModel dictionaryModels = dictionaryData.getAssociatedModels();
		SdaiIterator iterator = dictionaryModels.createIterator();
		boolean modelFound = false;
		while (iterator.next()) {
			dictionaryModel = dictionaryModels.getCurrentMember(iterator);
			if (dictionaryModel.getName().equals(schemaName)) {
				modelFound = true;
				break;
			}
		}
		if (!modelFound) {
			System.out.println("There is no data dictionary model for AUTOMOTIVE_DESIGN schema.");
			return;
		}

		dictionaryModel.startReadOnlyAccess();
		ESchema_definition dictionarySchema = dictionaryModel.getDefinedSchema();
		SdaiModel model = repo.createSdaiModel("Model1", dictionarySchema);
		model.startReadWriteAccess();

		EEntity_definition entityDefinition;
		EAttribute attribute;
		EDefined_type[] types = new EDefined_type[1];
		// This EDefined_type array should be big enough to hold all types in the select
		// path.
		// And this aggregate should be filled with them.

		entityDefinition = dictionarySchema.getEntityDefinition("application_context");
		EEntity app_context = model.createEntityInstance(entityDefinition);
		attribute = app_context.getAttributeDefinition("application");
		app_context.set(attribute, "mechanical design", null);

		entityDefinition = dictionarySchema.getEntityDefinition("application_protocol_definition");
		EEntity app_protocol = model.createEntityInstance(entityDefinition);
		attribute = app_protocol.getAttributeDefinition("status");
		app_protocol.set(attribute, "INTERNATIONAL STANDARD", null);
		attribute = app_protocol.getAttributeDefinition("application_interpreted_model_schema_name");
		app_protocol.set(attribute, "automotive design", null);
		attribute = app_protocol.getAttributeDefinition("application_protocol_year");
		app_protocol.set(attribute, 1994, null);
		attribute = app_protocol.getAttributeDefinition("application");
		app_protocol.set(attribute, app_context, null);

		entityDefinition = dictionarySchema.getEntityDefinition("product_context");
		EEntity pr_context = model.createEntityInstance(entityDefinition);
		attribute = pr_context.getAttributeDefinition("name");
		pr_context.set(attribute, "part definition", null);
		attribute = pr_context.getAttributeDefinition("frame_of_reference");
		pr_context.set(attribute, app_context, null);
		attribute = pr_context.getAttributeDefinition("discipline_type");
		pr_context.set(attribute, "mechanical", null);

		entityDefinition = dictionarySchema.getEntityDefinition("product");
		EEntity product = model.createEntityInstance(entityDefinition);
		attribute = product.getAttributeDefinition("id");
		product.set(attribute, "TestId", null);
		attribute = product.getAttributeDefinition("name");
		product.set(attribute, "TestName", null);
		attribute = product.getAttributeDefinition("description");
		product.set(attribute, "TestDescription", null);
		attribute = product.getAttributeDefinition("frame_of_reference");
		Aggregate contexts = product.createAggregate(attribute, null);
		contexts.addUnordered(pr_context, null);

		entityDefinition = dictionarySchema.getEntityDefinition("length_unit+si_unit");
		EEntity l_unit = model.createEntityInstance(entityDefinition);
		// The value 1 below is the index of the item ".METRE." in
		// ENUMERATION "si_unit_name", which is the type of the attribute "name".
		l_unit.set(l_unit.getAttributeDefinition("name"), 1, null);

		entityDefinition = dictionarySchema.getEntityDefinition("length_measure_with_unit+measure_representation_item");
		EEntity repr_item = model.createEntityInstance(entityDefinition);
		repr_item.set(repr_item.getAttributeDefinition("name"), "length measure", null);
		// Because attribute "value_component" is of select type and select path's
		// identification is needed, EDefined_type array "types" should be filled
		// with values (in fact, one value in this specific case).
		types[0] = dictionarySchema.getDefinedType("length_measure");
		repr_item.set(repr_item.getAttributeDefinition("value_component"), 1.0, types);
		repr_item.set(repr_item.getAttributeDefinition("unit_component"), l_unit, null);

		repo.exportClearTextEncoding("step-exp.stp");

		transaction.endTransactionAccessAbort();
		repo.closeRepository();
		repo.deleteRepository();

		System.out.println();
		System.out.println("Done");
		session.closeSession();
	}

}
