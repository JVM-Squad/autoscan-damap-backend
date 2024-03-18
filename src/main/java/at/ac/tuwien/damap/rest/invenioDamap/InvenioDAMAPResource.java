package at.ac.tuwien.damap.rest.invenioDamap;

import java.text.MessageFormat;
import java.util.*;
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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import at.ac.tuwien.damap.domain.Access;
import at.ac.tuwien.damap.repo.AccessRepo;
import at.ac.tuwien.damap.rest.dmp.domain.DatasetDO;
import at.ac.tuwien.damap.rest.dmp.domain.DmpDO;
import at.ac.tuwien.damap.rest.dmp.service.DmpService;
import at.ac.tuwien.damap.rest.madmp.dto.Dataset;
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

    private void checkIfUserIsAuthorized(String personId, HttpHeaders headers) {
        // This could allow us to authenticate users with OIDC, and allow admins to
        // access all DMPs.
        if (!(securityService.isAdmin() || Objects.equals(securityService.getUserId(), personId))) {
            // Since this will always evaluate to false as of right now, it will check the
            // auth header
            if (!validateAuthHeader(headers)) {
                throw new UnauthorizedException();
            }
        }
    }

    @GET
    @Path("/person/{personId}")
    public List<DmpDO> getDmpListByPerson(@PathParam String personId, @Context HttpHeaders headers) {
        log.info("Return dmp for person id: " + personId);

        checkIfUserIsAuthorized(personId, headers);

        List<Access> accessList = accessRepo.getAllDmpByUniversityId(personId);

        return accessList.stream().map(access ->
            dmpService.getDmpById(access.getDmp().id)
        ).toList();
    }

    @POST
    @Path("/{id}/{personId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public DmpDO addDataSetToDMP(@PathParam long id, @PathParam String personId, Dataset dataset,
                                 @Context HttpHeaders headers) {
        log.info("Add dataset to dmp with id: " + id);

        checkIfUserIsAuthorized(personId, headers);

        if (!accessValidator.canEditDmp(id, personId)) {
            throw new ForbiddenException("Not authorized to access dmp with id " + id);
        }

        DmpDO dmpDO = dmpService.getDmpById(id);
        var datasetDO = dmpDO.getDatasets().stream().filter(ds -> {
            var localIdentifier = ds.getDatasetId();
            var externalIdentifier = dataset.getDatasetId();

            if (localIdentifier == null || externalIdentifier == null) {
                return false;
            }

            return localIdentifier.getIdentifier() != null && externalIdentifier.getIdentifier() != null
                    && localIdentifier.getType() != null && externalIdentifier.getType() != null
                    && localIdentifier.getIdentifier().equals(externalIdentifier.getIdentifier())
                    && localIdentifier.getType().toString().equalsIgnoreCase(externalIdentifier.getType().name());
        }).findFirst().orElse(new DatasetDO());

        datasetDO = InvenioDamapResourceMapper.mapMaDMPDatasetToDatasetDO(dmpDO, datasetDO, dataset);

        dmpDO.getDatasets().add(datasetDO);
        dmpDO = dmpService.update(dmpDO);

        VersionDO version = new VersionDO();
        version.setDmpId(id);
        version.setVersionName(MessageFormat.format("Added dataset `{0}` from remote datasource", dataset.getTitle()));
        version.setVersionDate(new Date());
        versionService.create(version);

        return dmpDO;
    }
}
