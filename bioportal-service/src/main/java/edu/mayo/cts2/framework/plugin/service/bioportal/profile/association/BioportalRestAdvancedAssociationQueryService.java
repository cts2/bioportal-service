/*
 * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.association;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.AssociationTransform;
import edu.mayo.cts2.framework.service.profile.association.AdvancedAssociationQueryService;
import edu.mayo.cts2.framework.service.profile.entitydescription.id.EntityDescriptionId;
import edu.mayo.cts2.framework.model.association.AssociationGraph;
import edu.mayo.cts2.framework.model.association.GraphNode;
import edu.mayo.cts2.framework.model.association.types.GraphDirection;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.ModelAttributeReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.core.types.AssociationDirection;

/**
 * The Class BioportalRestAdvancedAssociationQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestAdvancedAssociationQueryService 
	extends AbstractBioportalRestQueryService<edu.mayo.cts2.framework.model.service.association.AssociationQueryService>
	implements AdvancedAssociationQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private IdentityConverter identityConverter;
	
	@Resource
	private AssociationTransform associationTransform;

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.association.AdvancedAssociationQueryService#getAssociationGraph(edu.mayo.cts2.sdk.service.profile.entitydescription.id.EntityDescriptionId, edu.mayo.cts2.framework.model.association.types.GraphDirection, long)
	 */
	public AssociationGraph getAssociationGraph(
			EntityDescriptionId id,
			GraphDirection direction,
			long depth) {	
		
		String codeSystemVersionName = id.getCodeSystemVersion();
		String codeSystemName = this.identityConverter.
				codeSystemVersionNameCodeSystemName(codeSystemVersionName);
		
		ScopedEntityName focusEntityName = id.getName();
		
		if(depth != 1){
			throw new UnsupportedOperationException("Only depth of '1' is allowed.");
		}
		if(direction != GraphDirection.FORWARD){
			throw new UnsupportedOperationException("Only GraphDirection of 'FORWARD' is allowed.");
		}
		
		AssociationGraph graph = new AssociationGraph();
		
		String ontologyVersionId = this.identityConverter.
			codeSystemVersionNameToOntologyVersionId(codeSystemVersionName);
		
		String xml;
		
		if(focusEntityName.getName().equals("TOP_NODE")){
			xml = bioportalRestService.getHierarchyRootsByOntolotyVersionId(ontologyVersionId);
		} else {
			xml = bioportalRestService.getEntityByOntologyVersionIdAndEntityId(ontologyVersionId, focusEntityName.getName());
		}
		
		List<GraphNode> associations = 
			this.associationTransform.transformAssociationForGraph(xml, codeSystemName, codeSystemVersionName);
		
		for(long i=0;i<associations.size();i++){
			GraphNode entry = associations.get((int)i);
			entry.setNodeNumber(i);
			entry.setNextNodeNumber(i+1);
			
			entry.setDirection(AssociationDirection.SOURCE_TO_TARGET);
			
			graph.addEntry(entry);
		}
		
		graph.setExpansionDepth(depth);
		graph.setNumEntries((long) associations.size());
		
		if(CollectionUtils.isNotEmpty(associations)){
			GraphNode focus = associations.get(0);
			graph.setFocusEntity(focus.getSubject());
		}
		
		graph.setExpansionDirection(direction);
		
		
		return graph;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerMatchAlgorithmReferences()
	 */
	@Override
	protected List<? extends MatchAlgorithmReference> getAvailableMatchAlgorithmReferences() {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerModelAttributeReferences()
	 */
	@Override
	protected List<? extends ModelAttributeReference> getAvailableModelAttributeReferences() {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerPredicateReferences()
	 */
	@Override
	protected List<? extends PredicateReference> getAvailablePredicateReferences() {
		return null;
	}
}
