package at.ac.tuwien.damap.rest.dmp.mapper;

import at.ac.tuwien.damap.domain.Dataset;
import at.ac.tuwien.damap.enums.EDataType;
import at.ac.tuwien.damap.rest.dmp.domain.DatasetDO;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DatasetDOMapper {

    public DatasetDO mapEntityToDO(Dataset dataset, DatasetDO datasetDO) {
        datasetDO.setId(dataset.id);
        datasetDO.setTitle(dataset.getTitle());
        datasetDO.setSize(dataset.getSize());
        datasetDO.setComment(dataset.getComment());
        datasetDO.setPersonalData(dataset.getPersonalData());
        datasetDO.setSensitiveData(dataset.getSensitiveData());
        datasetDO.setLegalRestrictions(dataset.getLegalRestrictions());
        datasetDO.setLicense(dataset.getLicense());
        datasetDO.setStartDate(dataset.getStart());
        datasetDO.setReferenceHash(dataset.getReferenceHash());
        datasetDO.setDelete(dataset.getDelete());
        datasetDO.setDateOfDeletion(dataset.getDateOfDeletion());
        datasetDO.setReasonForDeletion(dataset.getReasonForDeletion());
        datasetDO.setRetentionPeriod(dataset.getRetentionPeriod());
        if (dataset.getDataAccess() != null)
            datasetDO.setDataAccess(dataset.getDataAccess());
        if (dataset.getSelectedProjectMembersAccess() != null)
            datasetDO.setSelectedProjectMembersAccess(dataset.getSelectedProjectMembersAccess());
        if (dataset.getOtherProjectMembersAccess() != null)
            datasetDO.setOtherProjectMembersAccess(dataset.getOtherProjectMembersAccess());
        if (dataset.getPublicAccess() != null)
            datasetDO.setPublicAccess(dataset.getPublicAccess());

        List<EDataType> typeList = new ArrayList<>();
        dataset.getType().forEach(option -> {
            if (option != null) {
                typeList.add(option);
            }
        });
        datasetDO.setType(typeList);

        return datasetDO;
    }

    public Dataset mapDOtoEntity(DatasetDO datasetDO, Dataset dataset){
        if (datasetDO.getId() != null)
            dataset.id = datasetDO.getId();
        dataset.setTitle(datasetDO.getTitle());
        dataset.setSize(datasetDO.getSize());
        dataset.setComment(datasetDO.getComment());
        dataset.setPersonalData(datasetDO.getPersonalData());
        dataset.setSensitiveData(datasetDO.getSensitiveData());
        dataset.setLegalRestrictions(datasetDO.getLegalRestrictions());
        dataset.setLicense(datasetDO.getLicense());
        dataset.setStart(datasetDO.getStartDate());
        dataset.setReferenceHash(datasetDO.getReferenceHash());
        dataset.setDataAccess(datasetDO.getDataAccess());
        dataset.setSelectedProjectMembersAccess(datasetDO.getSelectedProjectMembersAccess());
        dataset.setOtherProjectMembersAccess(datasetDO.getOtherProjectMembersAccess());
        dataset.setPublicAccess(datasetDO.getPublicAccess());
        dataset.setDelete(datasetDO.getDelete());
        dataset.setDateOfDeletion(datasetDO.getDateOfDeletion());
        dataset.setReasonForDeletion(datasetDO.getReasonForDeletion());
        dataset.setRetentionPeriod(datasetDO.getRetentionPeriod());

        List<EDataType> typeList = new ArrayList<>();
        datasetDO.getType().forEach(option -> {
            if (option != null) {
                typeList.add(option);
            }
        });
        dataset.setType(typeList);

        return dataset;
    }}
