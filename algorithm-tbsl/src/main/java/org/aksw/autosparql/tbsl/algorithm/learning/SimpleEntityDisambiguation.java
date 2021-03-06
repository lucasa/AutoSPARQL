package org.aksw.autosparql.tbsl.algorithm.learning;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.aksw.autosparql.commons.knowledgebase.Knowledgebase;
import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.sparql.SlotType;
import org.aksw.autosparql.tbsl.algorithm.sparql.Template;
import org.aksw.rdfindex.Index;
import org.aksw.rdfindex.IndexItem;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

public class SimpleEntityDisambiguation {

	private static final Logger logger = Logger.getLogger(SimpleEntityDisambiguation.class.getName());

	private Knowledgebase knowledgebase;
	private SimpleIRIShortFormProvider iriSfp = new SimpleIRIShortFormProvider();

	public SimpleEntityDisambiguation(Knowledgebase knowledgebase) {
		this.knowledgebase = knowledgebase;
	}

	public Map<Template, Map<Slot, Collection<Entity>>> performEntityDisambiguation(Collection<Template> templates){
		Map<Template, Map<Slot, Collection<Entity>>> template2Allocations = new HashMap<Template, Map<Slot,Collection<Entity>>>();

		for(Template template : templates){
			Map<Slot, Collection<Entity>> slot2Entities = performEntityDisambiguation(template);
			template2Allocations.put(template, slot2Entities);
		}
		return template2Allocations;
	}

	public Map<Slot, Collection<Entity>> performEntityDisambiguation(Template template){
		Map<Slot, Collection<Entity>> slot2Entities = new HashMap<Slot, Collection<Entity>>();
		List<Slot> slots = template.getSlots();
		for(Slot slot : slots){
			Collection<Entity> candidateEntities = getCandidateEntities(slot);
			slot2Entities.put(slot, candidateEntities);
		}
		return slot2Entities;
	}

	/** get sorted list of entities
	 */
	private Collection<Entity> getCandidateEntities(Slot slot){
		logger.trace("Generating entity candidates for slot " + slot + "...");
		Set<Entity> candidateEntities = new HashSet<Entity>();
//		if(slot.getSlotType() == SlotType.RESOURCE){
//			List<String> words = slot.getWords();
//			List<Resource> uriCandidates = new ArrayList<Resource>();
//			for(String word : words){
//				uriCandidates.addAll(UriDisambiguation.getTopUris(UriDisambiguation.getUriCandidates(word, "en"), word, "en"));
//			}
//			for (Resource resource : uriCandidates) {
//				candidateEntities.add(new Entity(resource.uri, resource.label));
//			}
//		} else {
			Index index = getIndexForSlot(slot);
			List<String> words = slot.getWords();
			for(String word : words){

				// disable system.out
				PrintStream out = System.out;
				System.setOut(new PrintStream(new OutputStream() {@Override public void write(int arg0) throws IOException {}}));
				SortedSet<IndexItem> items = index.getResourcesWithScores(word, 10);
				// enable again
				System.setOut(out);
				for(IndexItem item : items){
					String uri = item.getUri();
					String label = item.getLabel();
					if(label == null){
						label = iriSfp.getShortForm(IRI.create(uri));
					}
					candidateEntities.add(new Entity(uri, label));
				}
			}
//		}
		logger.debug("Found " + candidateEntities.size() + " entities for slot "+slot+": "+candidateEntities);
		return candidateEntities;
	}

	private Index getIndexForSlot(Slot slot){
		Index index = null;
		SlotType type = slot.getSlotType();
		if(type == SlotType.CLASS){
			index = knowledgebase.getIndices().getClassIndex();
		} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY){
			index = knowledgebase.getIndices().getPropertyIndex();
		} else if(type == SlotType.DATATYPEPROPERTY){
			index = knowledgebase.getIndices().getDataPropertyIndex();
		} else if(type == SlotType.OBJECTPROPERTY){
			index = knowledgebase.getIndices().getObjectPropertyIndex();
		} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
			index = knowledgebase.getIndices().getResourceIndex();
		}
		return index;
	}

}