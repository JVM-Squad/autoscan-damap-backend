package at.ac.tuwien.damap.rest.mapper;

import at.ac.tuwien.damap.domain.*;
import at.ac.tuwien.damap.rest.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DmpDOMapper {

    public static void mapEntityToDO(Dmp dmp, DmpDO dmpDO) {
        dmpDO.setId(dmp.id);
        dmpDO.setTitle(dmp.getTitle());
        dmpDO.setCreated(dmp.getCreated());
        dmpDO.setModified(dmp.getModified());
        dmpDO.setDescription(dmp.getDescription());

        if (dmp.getProject() != null) {
            ProjectDO projectDO = new ProjectDO();
            ProjectDOMapper.mapEntityToDO(dmp.getProject(), projectDO);
            dmpDO.setProject(projectDO);
        }

        if (dmp.getContact() != null) {
            PersonDO contactDO = new PersonDO();
            PersonDOMapper.mapEntityToDO(dmp.getContact(), contactDO);
            dmpDO.setContact(contactDO);
        }

        dmpDO.setDataKind(dmp.getDataKind());
        dmpDO.setNoDataExplanation(dmp.getNoDataExpalnation());
        dmpDO.setMetadata(dmp.getMetadata());
        dmpDO.setDataGeneration(dmp.getDataGeneration());
        dmpDO.setStructure(dmp.getStructure());
        dmpDO.setTargetAudience(dmp.getTargetAudience());
        dmpDO.setPersonalInformation(dmp.getPersonalInformation());
        dmpDO.setSensitiveData(dmp.getSensitiveData());
        dmpDO.setLegalRestrictions(dmp.getLegalRestrictions());
        dmpDO.setEthicalIssuesExist(dmp.getEthicalIssuesExist());
        dmpDO.setCommitteeApproved(dmp.getCommitteeApproved());
        dmpDO.setEthicsReport(dmp.getEthicsReport());
        dmpDO.setOptionalStatement(dmp.getOptionalStatement());

        List<ContributorDO> contributorDOList = new ArrayList<>();
        dmp.getContributorList().forEach(contributor -> {
            ContributorDO contributorDO = new ContributorDO();
            ContributorDOMapper.mapEntityToDO(contributor, contributorDO);
            contributorDOList.add(contributorDO);
        });
        dmpDO.setContributors(contributorDOList);

        List<DatasetDO> datasetDOList = new ArrayList<>();
        dmp.getDatasetList().forEach(dataset -> {
            DatasetDO datasetDO = new DatasetDO();
            DatasetDOMapper.mapEntityToDO(dataset, datasetDO);
            datasetDOList.add(datasetDO);
        });
        dmpDO.setDatasets(datasetDOList);

        List<HostDO> hostDOList = new ArrayList<>();
        dmp.getHostList().forEach(host -> {
            HostDO hostDO = new HostDO();
            HostDOMapper.mapEntityToDO(host, hostDO);
            hostDOList.add(hostDO);

            //add frontend referenceHash list to host
            List<String> referenceHashList = new ArrayList<>();
            host.getDatasetList().forEach(dataset -> {
                referenceHashList.add(dataset.getReferenceHash());
            });
            if (!referenceHashList.isEmpty())
                hostDO.setDatasets(referenceHashList);

        });
        dmpDO.setHosts(hostDOList);
    }

    public static void mapDOtoEntity(DmpDO dmpDO, Dmp dmp) {
        if (dmpDO.getId() != null)
            dmp.id = dmpDO.getId();
        dmp.setTitle(dmpDO.getTitle());
        dmp.setCreated(dmpDO.getCreated());
        dmp.setModified(dmpDO.getModified());
        dmp.setDescription(dmpDO.getDescription());

        if (dmpDO.getProject() != null) {
            Project project = new Project();
            if (dmp.getProject() != null)
                project = dmp.getProject();
            ProjectDOMapper.mapDOtoEntity(dmpDO.getProject(), project);
            dmp.setProject(project);
        } else
            dmp.setProject(null);

        if (dmpDO.getContact() != null) {
            Person contact = new Person();
            if (dmp.getContact() != null)
                contact = dmp.getContact();
            PersonDOMapper.mapDOtoEntity(dmpDO.getContact(), contact);
            dmp.setContact(contact);
        } else
            dmp.setContact(null);

        dmp.setDataKind(dmpDO.getDataKind());
        dmp.setNoDataExpalnation(dmpDO.getNoDataExplanation());
        dmp.setMetadata(dmpDO.getMetadata());
        dmp.setDataGeneration(dmpDO.getDataGeneration());
        dmp.setStructure(dmpDO.getStructure());
        dmp.setTargetAudience(dmpDO.getTargetAudience());
        dmp.setPersonalInformation(dmpDO.getPersonalInformation());
        dmp.setSensitiveData(dmpDO.getSensitiveData());
        dmp.setLegalRestrictions(dmpDO.getLegalRestrictions());
        dmp.setEthicalIssuesExist(dmpDO.getEthicalIssuesExist());
        dmp.setCommitteeApproved(dmpDO.getCommitteeApproved());
        dmp.setEthicsReport(dmpDO.getEthicsReport());
        dmp.setOptionalStatement(dmpDO.getOptionalStatement());


        //TODO also check for existing contributors based on Identifier, not just universityId

        //remove all existing Contributor objects, that are not included in the DO anymore
        List<Contributor> contributorList = dmp.getContributorList();
        List<Contributor> contributorListToRemove = new ArrayList<>();
        contributorList.forEach(contributor -> {
            Optional<ContributorDO> contributorDOOptional = dmpDO.getContributors().stream().filter(contributorDO ->
                    contributorDO.getPerson().getUniversityId().equals(contributor.getContributor().getUniversityId())).findFirst();
            if (contributorDOOptional.isEmpty()) {
                contributorListToRemove.add(contributor);
            }
        });
        contributorList.removeAll(contributorListToRemove);

        //update existing Contributor objects and create new ones
        dmpDO.getContributors().forEach(contributorDO -> {
            Optional<Contributor> contributorOptional = contributorList.stream().filter(contributor ->
                    contributorDO.getPerson().getUniversityId().equals(contributor.getContributor().getUniversityId())).findFirst();
            if (contributorOptional.isPresent()) {
                Contributor contributor = contributorOptional.get();
                ContributorDOMapper.mapDOtoEntity(contributorDO, contributor);
            } else {
                Contributor contributor = new Contributor();
                ContributorDOMapper.mapDOtoEntity(contributorDO, contributor);
                contributor.setDmp(dmp);
                contributorList.add(contributor);
            }
        });


        //remove all existing Dataset objects, that are not included in the DO anymore
        List<Dataset> datasetList = dmp.getDatasetList();
        List<Dataset> datasetListToRemove = new ArrayList<>();
        datasetList.forEach(dataset -> {
            Optional<DatasetDO> datasetDOOptional = dmpDO.getDatasets().stream().filter(datasetDO ->
                    datasetDO.getReferenceHash().equals(dataset.getReferenceHash())).findFirst();
            if (datasetDOOptional.isEmpty()) {
                datasetListToRemove.add(dataset);
            }
        });
        datasetList.removeAll(datasetListToRemove);

        //update existing Dataset objects and create new ones
        dmpDO.getDatasets().forEach(datasetDO -> {
            Optional<Dataset> datasetOptional = datasetList.stream().filter(dataset ->
                    datasetDO.getReferenceHash().equals(dataset.getReferenceHash())).findFirst();
            if (datasetOptional.isPresent()) {
                Dataset dataset = datasetOptional.get();
                DatasetDOMapper.mapDOtoEntity(datasetDO, dataset);
            } else {
                Dataset dataset = new Dataset();
                DatasetDOMapper.mapDOtoEntity(datasetDO, dataset);
                dataset.setDmp(dmp);
                datasetList.add(dataset);
            }
        });


        //remove all existing Host objects, that are not included in the DO anymore
        List<Host> hostList = dmp.getHostList();
        List<Host> hostListToRemove = new ArrayList<>();
        hostList.forEach(host -> {
            Optional<HostDO> hostDOOptional = dmpDO.getHosts().stream().filter(hostDO ->
                    hostDO.getHostId().equals(host.getHostId())).findFirst();
            if (hostDOOptional.isEmpty()) {
                hostListToRemove.add(host);
            }
        });
        hostList.removeAll(hostListToRemove);

        //update existing Host objects and create new ones
        dmpDO.getHosts().forEach(hostDO -> {
            Optional<Host> hostOptional = hostList.stream().filter(host ->
                    hostDO.getHostId().equals(host.getHostId())).findFirst();
            Host host;
            if (hostOptional.isPresent()) {
                host = hostOptional.get();
                HostDOMapper.mapDOtoEntity(hostDO, host);
            } else {
                host = new Host();
                HostDOMapper.mapDOtoEntity(hostDO, host);
                host.setDmp(dmp);
                hostList.add(host);
            }

            //convert datasetHash to id references from datasdet to hosts
            dmp.getDatasetList().forEach(dataset -> {
                if (hostDO.getDatasets().contains(dataset.getReferenceHash())){
                    dataset.setHost(host);
                }
            });
        });
    }
}