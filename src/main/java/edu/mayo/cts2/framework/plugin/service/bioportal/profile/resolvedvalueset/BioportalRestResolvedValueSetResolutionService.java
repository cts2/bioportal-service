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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.resolvedvalueset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.EntitySynopsis;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.core.PropertyReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet;
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetHeader;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.entitydescription.BioportalRestEntityDescriptionQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestUtils;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ResolvedValueSetTransform;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.TransformUtils;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntitiesFromAssociationsQuery;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.ResolvedValueSetResolutionService;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.name.ResolvedValueSetReadId;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResult;

/**
 * The Class BioportalRdfResolvedValueSetResolutionService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestResolvedValueSetResolutionService extends AbstractBioportalRestService 
	implements ResolvedValueSetResolutionService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ResolvedValueSetTransform resolvedValueSetTransform;
	
	@Resource
	private IdentityConverter identityConverter;
	
	@Resource
	private BioportalRestEntityDescriptionQueryService bioportalRestEntityDescriptionQueryService;
	
	@Override
	public ResolvedValueSetResult getResolution(
			ResolvedValueSetReadId identifier,
			final Set<ResolvedFilter> filterComponent, 
			Page page) {
		String ontologyId = this.identityConverter.valueSetNameToOntologyId(
				identifier.getValueSet().getName());
		
		String ontologyVersionId = identifier.getLocalName();
		
		final String valueSetDefinitionName = 
				this.identityConverter.ontologyVersionIdToValueSetDefinitionName(
					ontologyId, 
					ontologyVersionId);
		
		
		EntityDescriptionQuery query = new EntityDescriptionQuery(){

			@Override
			public Query getQuery() {
				return null;
			}

			@Override
			public Set<ResolvedFilter> getFilterComponent() {
				return filterComponent;
			}

			@Override
			public ResolvedReadContext getReadContext() {
				return null;
			}

			@Override
			public EntityDescriptionQueryServiceRestrictions getRestrictions() {
				EntityDescriptionQueryServiceRestrictions restrictions = 
						new EntityDescriptionQueryServiceRestrictions();
				
				restrictions.setCodeSystemVersion(
						ModelUtils.nameOrUriFromName(valueSetDefinitionName));
				
				return restrictions;
			}

			@Override
			public EntitiesFromAssociationsQuery getEntitiesFromAssociationsQuery() {
				return null;
			}
		};
		
		DirectoryResult<EntityDirectoryEntry> entities = 
				this.bioportalRestEntityDescriptionQueryService.getResourceSummaries(
					query, 
					null, 
					page);

		ResolvedValueSetHeader header = this.getResolvedValueSetHeader(ontologyVersionId);
		
		return this.toResolvedValueSetResult(entities, header);

	}
	
	private ResolvedValueSetHeader getResolvedValueSetHeader(String ontologyVersionId){
		String xml = this.bioportalRestService.getOntologyByOntologyVersionId(ontologyVersionId);
		
		Document doc = BioportalRestUtils.getDocument(xml);
		
		Node node = TransformUtils.getNamedChildWithPath(doc, "success.data.ontologyBean");
		
		return this.resolvedValueSetTransform.getHeader(node);
	}
	
	protected ResolvedValueSetResult toResolvedValueSetResult(DirectoryResult<EntityDirectoryEntry> entities, ResolvedValueSetHeader header){
		List<EntitySynopsis> list = new ArrayList<EntitySynopsis>();
		for(EntityDirectoryEntry entry : entities.getEntries()){
			list.add(this.entityDirectoryEntryToEntitySynopsis(entry));
		}

		return new ResolvedValueSetResult(header, list, entities.isAtEnd());
	}
	
	protected EntitySynopsis entityDirectoryEntryToEntitySynopsis(EntityDirectoryEntry entry){
		EntitySynopsis synopsis = new EntitySynopsis();
		synopsis.setHref(entry.getHref());
		synopsis.setDesignation(entry.getKnownEntityDescription(0).getDesignation());
		synopsis.setName(entry.getName().getName());
		synopsis.setNamespace(entry.getName().getNamespace());
		synopsis.setUri(entry.getAbout());
		
		return synopsis;
	}

	@Override
	public ResolvedValueSet getResolution(
			ResolvedValueSetReadId identifier) {
		throw new UnsupportedOperationException("Cannot resolve the complete ResolvedValueSet yet...");
	}

	@Override
	public Set<? extends PropertyReference> getSupportedSortReferences() {
		return null;
	}

	@Override
	public Set<PredicateReference> getKnownProperties() {
		return null;
	}

	@Override
	public Set<? extends MatchAlgorithmReference> getSupportedMatchAlgorithms() {
		HashSet<MatchAlgorithmReference> returnSet = new HashSet<MatchAlgorithmReference>();
		
		returnSet.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference());
		returnSet.add(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference());

		return returnSet;
	}

	@Override
	public Set<? extends PropertyReference> getSupportedSearchReferences() {
		HashSet<PropertyReference> returnSet = new HashSet<PropertyReference>();

		returnSet.add(StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference());
		
		return returnSet;
	}	
}
