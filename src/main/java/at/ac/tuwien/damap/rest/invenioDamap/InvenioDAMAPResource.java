package at.ac.tuwien.damap.rest.invenioDamap;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import at.ac.tuwien.damap.domain.Access;
import at.ac.tuwien.damap.enums.EAccessRight;
import at.ac.tuwien.damap.enums.EDataAccessType;
import at.ac.tuwien.damap.enums.EDataKind;
import at.ac.tuwien.damap.enums.EDataSource;
import at.ac.tuwien.damap.enums.EDataType;
import at.ac.tuwien.damap.enums.EIdentifierType;
import at.ac.tuwien.damap.enums.ELicense;
import at.ac.tuwien.damap.repo.AccessRepo;
import at.ac.tuwien.damap.rest.dmp.domain.DatasetDO;
import at.ac.tuwien.damap.rest.dmp.domain.DmpDO;
import at.ac.tuwien.damap.rest.dmp.domain.ExternalStorageDO;
import at.ac.tuwien.damap.rest.dmp.domain.IdentifierDO;
import at.ac.tuwien.damap.rest.dmp.mapper.MapperService;
import at.ac.tuwien.damap.rest.dmp.service.DmpService;
import at.ac.tuwien.damap.rest.madmp.dto.Dataset;
import at.ac.tuwien.damap.rest.madmp.dto.Host;
import at.ac.tuwien.damap.rest.madmp.service.MaDmpService;
import at.ac.tuwien.damap.rest.version.VersionDO;
import at.ac.tuwien.damap.rest.version.VersionService;
import at.ac.tuwien.damap.security.SecurityService;
import at.ac.tuwien.damap.validation.AccessValidator;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.jbosslog.JBossLog;

@Path("/api/invenio-damap/dmps")
@Produces(MediaType.APPLICATION_JSON)
@JBossLog
public class InvenioDAMAPResource {

    @Inject
    SecurityService securityService;

    @Inject
    AccessValidator accessValidator;

    @Inject
    AccessRepo accessRepo;

    @Inject
    DmpService dmpService;

    @Inject
    MaDmpService madmpService;

    @Inject
    VersionService versionService;

    @ConfigProperty(name = "invenio.shared-secret")
    String sharedSecret;

    /*
     * Maybe make it configurable?
     * Could be more elaborate but should suffice for PoC
     * Could also go into the {@link at.ac.tuwien.damap.security.SecurityService},
     * but keeping everything together for PoC
     * 
     */
    private boolean validateAuthHeader(HttpHeaders headers) {
        return sharedSecret.equals(headers.getHeaderString("Authorization"));
    }

    @GET
    @Path("/person/{personId}")
    public List<DmpDO> getDmpListByPerson(@PathParam String personId,
            @Context HttpHeaders headers) {
        log.info("Return dmp for person id: " + personId);

        // This could allow us to authenticate users with OIDC, and allow admins to
        // access all DMPs.
        if (!(securityService.isAdmin() || securityService.getUserId() == personId)) {
            // Since this will always evaluate to false as of right now, it will check the
            // auth header
            if (!validateAuthHeader(headers)) {
                throw new UnauthorizedException();
            }
        }

        List<Access> accessList = accessRepo.getAllDmpByUniversityId(personId);

        return accessList.stream().map(access -> {
            return dmpService.getDmpById(access.getDmp().id);
        }).collect(Collectors.toList());
    }

    @POST
    @Path("/{id}/{personId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public DmpDO addDataSetToDMP(@PathParam long id, @PathParam String personId, Dataset dataset,
            @Context HttpHeaders headers) {
        log.info("Add dataset to dmp with id: " + id);

        if (!(securityService.isAdmin() || securityService.getUserId() == personId)) {
            if (!validateAuthHeader(headers)) {
                throw new UnauthorizedException();
            }
        }

        if (!accessValidator.canEditDmp(id, personId)) {
            throw new ForbiddenException("Not authorized to access dmp with id " +
                    id);
        }

        DmpDO dmpDO = dmpService.getDmpById(id);
        var datasetDO = mapMaDMPDatasetToDatasetDO(dmpDO, new DatasetDO(), dataset, null);

        dmpDO.getDatasets().add(datasetDO);
        dmpDO = dmpService.update(dmpDO);

        VersionDO version = new VersionDO();
        version.setDmpId(id);
        version.setVersionName(MessageFormat.format("Added dataset `{0}` from remote datasource", dataset.getTitle()));
        version.setVersionDate(new Date());
        versionService.create(version);

        return dmpDO;
    }

    // TODO: move to mapper
    private DatasetDO mapMaDMPDatasetToDatasetDO(DmpDO dmpDO,
            DatasetDO datasetDO,
            Dataset madmpDataset, MapperService mapperService) {

        // Disclaimer: This is by no means complete. Not all fields of the
        // Dataset or DMP are set. Null value checks should also be performed.
        var datasetId = madmpDataset.getDatasetId();
        if (datasetId != null) {
            IdentifierDO newId = new IdentifierDO();
            newId.setIdentifier(datasetId.getIdentifier());
            newId.setType(EIdentifierType.valueOf(datasetId.getType().name().toUpperCase()));
            datasetDO.setDatasetId(newId);
        }

        // TODO: should we set this? If so, what to ?
        datasetDO.setReferenceHash(RandomStringUtils.randomAlphanumeric(64));
        datasetDO.setDateOfDeletion(null);
        datasetDO.setDelete(false);
        datasetDO.setDeletionPerson(null);
        datasetDO.setDescription(madmpDataset.getDescription());
        datasetDO.setLegalRestrictions(null);
        datasetDO.setLicense(null);
        datasetDO.setSize(0L);
        // General TODO: some attributes have to be set from distribution
        if (madmpDataset.getDistribution() != null) {
            var distributions = madmpDataset.getDistribution();
            StringBuilder licenseBuilder = new StringBuilder();
            distributions.forEach(d -> {
                var dataAccess = EDataAccessType.OPEN;
                if (d.getDataAccess() != null) {
                    dataAccess = EDataAccessType.getByValue(d.getDataAccess().value());
                }
                datasetDO.setDataAccess(dataAccess);
                licenseBuilder.append(d.getLicense().stream()
                        .map(l -> l.getLicenseRef().toString()).collect(Collectors.joining(", ")));
                datasetDO.setSize(datasetDO.getSize() + d.getByteSize());

                if (d.getHost() != null) {
                    Host host = d.getHost();

                    ExternalStorageDO externalStorageDO = null;
                    String hostPath = host.getUrl() == null ? null : host.getUrl().getPath();

                    var externalStorages = dmpDO.getExternalStorage();
                    if (hostPath != null) {
                        externalStorageDO = externalStorages.stream()
                                .filter(s -> hostPath.equals(s.getUrl())).findFirst()
                                .orElse(null);
                    }
                    if (externalStorageDO == null) {
                        externalStorageDO = new ExternalStorageDO();
                        externalStorageDO.setBackupFrequency(host.getBackupFrequency());
                        externalStorageDO.setStorageLocation(
                                host.getGeoLocation() != null ? host.getGeoLocation().toString() : null);
                        externalStorageDO.setTitle(host.getTitle());
                        externalStorageDO.setUrl(hostPath);
                        externalStorages.add(externalStorageDO);
                    }

                    var datasetHashes = externalStorageDO.getDatasets();
                    datasetHashes.add(datasetDO.getReferenceHash());
                    externalStorageDO.setDatasets(datasetHashes);

                    dmpDO.setExternalStorage(externalStorages);
                }
            });

            // TODO: Support multiple licenses
            ELicense license = Arrays.stream(ELicense.values())
                    .filter(eLicense -> eLicense.getUrl().equals(licenseBuilder.toString()))
                    .findFirst()
                    .orElse(null);
            datasetDO.setLicense(license);
        }

        datasetDO.setOtherProjectMembersAccess(EAccessRight.READ);

        Boolean personalData = true;
        switch (madmpDataset.getPersonalData()) {
            case NO:
                personalData = false;
                break;
            case UNKNOWN:
            case YES:
            default:
                personalData = true;
                break;
        }
        datasetDO.setPersonalData(personalData);
        dmpDO.setPersonalData(dmpDO.getPersonalData() || personalData);

        datasetDO.setPublicAccess(EAccessRight.READ);
        datasetDO.setReasonForDeletion("");

        datasetDO.setRetentionPeriod(null);
        datasetDO.setSelectedProjectMembersAccess(EAccessRight.READ);

        Boolean sensitiveData = true;
        switch (madmpDataset.getSensitiveData()) {
            case NO:
                sensitiveData = false;
                break;
            case UNKNOWN:
            case YES:
            default:
                sensitiveData = true;
                break;
        }
        datasetDO.setSensitiveData(sensitiveData);
        dmpDO.setSensitiveData(dmpDO.getSensitiveData() || sensitiveData);

        // TODO: Let user decide?
        datasetDO.setSource(EDataSource.NEW);
        // This should match the dataset. If new, setDataKind. else setReusedDataKind
        dmpDO.setDataKind(EDataKind.SPECIFY);
        dmpDO.setReusedDataKind(EDataKind.SPECIFY);
        datasetDO.setStartDate(null);
        datasetDO.setTitle(madmpDataset.getTitle());
        // Setting data type
        var types = new ArrayList<EDataType>();
        var type = EDataType.OTHER;
        try {
            type = EDataType.getByValue(madmpDataset.getType());
        } catch (Exception e) {

        } finally {
            types.add(type);
            datasetDO.setType(types);
        }

        return datasetDO;
    }
}
