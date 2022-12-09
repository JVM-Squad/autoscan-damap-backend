package at.ac.tuwien.damap.rest.invenioDamap;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import at.ac.tuwien.damap.rest.dmp.domain.DmpListItemDO;
import at.ac.tuwien.damap.rest.dmp.service.DmpService;
import at.ac.tuwien.damap.security.SecurityService;
import at.ac.tuwien.damap.validation.AccessValidator;
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
    DmpService dmpService;

    @ConfigProperty(name = "invenio.shared-secret")
    String sharedSecret;

    // ADMIN

    // @GET
    // @Path("/all")
    // @RolesAllowed("Damap Admin")
    // public List<DmpListItemDO> getAll() {
    // log.info("Return all Dmps");
    // return dmpService.getAll();
    // }

    private boolean validateAuthHeader(HttpHeaders headers) {
        // Maybe make it configurable?
        // Could be more elaborate but should suffice for PoC
        // Could also go into the {@link at.ac.tuwien.damap.security.SecurityService},
        // but keeping everything together for PoC
        return sharedSecret.equals(headers.getHeaderString("Authorization"));
    }

    @GET
    @Path("/person/{personId}")
    // @RolesAllowed("Damap Admin")
    public List<DmpListItemDO> getDmpListByPerson(@PathParam String personId, @Context HttpHeaders headers) {
        log.info("Return dmp for person id: " + personId);

        // This could allow us to authenticate users with OIDC, and allow admins to
        // access all DMPs.
        // Since this will always evaluate to false as of right now, it will check the
        // auth header
        if (!(securityService.isAdmin() || securityService.getUserId() == personId)) {
            if (!validateAuthHeader(headers)) {
                throw new UnauthorizedException();
            }
        }

        return dmpService.getDmpListByPersonId(personId);
    }

    // USER

    // @GET
    // @Path("/list")
    // public List<DmpListItemDO> getDmpList() {
    // log.info("Return dmp list for user");
    // String personId = this.getPersonId();
    // log.info("User id: " + personId);
    // return dmpService.getDmpListByPersonId(personId);
    // }

    /*
     * @GET
     * 
     * @Path("/subordinates")
     * 
     * @RolesAllowed("user")
     * public List<DmpListItemDO> getDmpsSubordinates() {
     * log.info("Return dmp list for subordinates");
     * String personId = this.getPersonId();
     * log.info("User id: " + personId);
     * // TODO: Service stub
     * return dmpService.getDmpListByPersonId(personId);
     * }
     */

    // @GET
    // @Path("/{id}")
    // public DmpDO getDmpById(@PathParam String id) {
    // log.info("Return dmp with id: " + id);
    // String personId = this.getPersonId();
    // long dmpId = Long.parseLong(id);
    // if(!accessValidator.canViewDmp(dmpId, personId)){
    // throw new ForbiddenException("Not authorized to access dmp with id " +
    // dmpId);
    // }
    // return dmpService.getDmpById(dmpId);
    // }

    // @POST
    // @Consumes(MediaType.APPLICATION_JSON)
    // public DmpDO saveDmp(@Valid DmpDO dmpDO) {
    // log.info("Save dmp");
    // String personId = this.getPersonId();
    // return dmpService.create(dmpDO, personId);
    // }

    // @PUT
    // @Path("/{id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    // public DmpDO updateDmp(@PathParam String id, @Valid DmpDO dmpDO) {
    // log.info("Update dmp with id: " + id);
    // String personId = this.getPersonId();
    // long dmpId = Long.parseLong(id);
    // if(!accessValidator.canEditDmp(dmpId, personId)){
    // throw new ForbiddenException("Not authorized to access dmp with id " +
    // dmpId);
    // }
    // return dmpService.update(dmpDO);
    // }

    // @DELETE
    // @Path("/{id}")
    // public void deleteDmp(@PathParam String id) {
    // log.info("Delete dmp with id: " + id);
    // String personId = this.getPersonId();
    // long dmpId = Long.parseLong(id);
    // if (!accessValidator.canDeleteDmp(dmpId, personId)) {
    // throw new ForbiddenException("Not authorized to delete dmp with id " +
    // dmpId);
    // }
    // dmpService.delete(dmpId);
    // }

    // private String getPersonId() {
    // if (securityService == null) {
    // throw new AuthenticationFailedException("User ID is missing.");
    // }
    // return securityService.getUserId();
    // }

    // @GET
    // @Path("/{id}/{revision}")
    // public DmpDO getDmpByIdAndRevision(@PathParam String id, @PathParam long
    // revision) {
    // log.info("Return dmp with id: " + id + " and revision number: " + revision);
    // String personId = this.getPersonId();
    // long dmpId = Long.parseLong(id);
    // if(!accessValidator.canViewDmp(dmpId, personId)){
    // throw new ForbiddenException("Not authorized to access dmp with id " +
    // dmpId);
    // }
    // return dmpService.getDmpByIdAndRevision(dmpId, revision);
    // }
}
