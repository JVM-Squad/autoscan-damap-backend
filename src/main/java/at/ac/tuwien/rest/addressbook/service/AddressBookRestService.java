package at.ac.tuwien.rest.addressbook.service;

import at.ac.tuwien.rest.addressbook.dto.PersonDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/api")
@RegisterRestClient
public interface AddressBookRestService {

    @GET
    @Path("/person/v22/id/{id}")
    @Produces("application/json")
    PersonDTO getPersonDetailsById(@PathParam String id);
}