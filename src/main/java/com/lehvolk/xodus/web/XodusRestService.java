package com.lehvolk.xodus.web;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.lehvolk.xodus.web.services.PersistentStoreService;
import com.lehvolk.xodus.web.vo.ChangeSummaryVO;
import com.lehvolk.xodus.web.vo.EntityTypeVO;
import com.lehvolk.xodus.web.vo.EntityVO;
import com.lehvolk.xodus.web.vo.SearchPagerVO;
import jetbrains.exodus.entitystore.EntityStoreException;
import lombok.extern.slf4j.Slf4j;
import static java.lang.Math.min;

/**
 * REST service for PersistentStore
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Slf4j
@Path("/")
public class XodusRestService {

    @Inject
    private PersistentStoreService persistentStoreService;

    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public EntityTypeVO[] getAllTypes() {
        log.debug("getting all entity types");
        try {
            return persistentStoreService.getTypes();
        } catch (EntityStoreException e) {
            log.error("error getting all types", e);
            throw new NotFoundException(e);
        }
    }

    @GET
    @Path("/type/{id}/entities")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchPagerVO searchEntities(
            @PathParam("id") int id,
            @QueryParam("q") String term,
            @QueryParam("offset") int offset,
            @QueryParam("pageSize") int pageSize) {
        log.debug("searching entities by typeId: {}, term [{}] with offset = {} and pageSize = {}",
                id, term, offset, pageSize);
        if (offset < 0 || pageSize < 0) {
            throw new BadRequestException();
        }
        try {
            return persistentStoreService.searchType(id, term, offset, (pageSize == 0) ? 50 : min(pageSize, 1000));
        } catch (EntityStoreException e) {
            log.error("error searching entities", e);
            throw new NotFoundException(e);
        }
    }

    @GET
    @Path("/type/{id}/entity/{entityId}")
    @Produces(MediaType.APPLICATION_JSON)
    public EntityVO getEntity(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId) {
        log.debug("getting entity by typeId={} and entityId={}", id, entityId);
        try {
            return persistentStoreService.getEntity(id, entityId);
        } catch (EntityStoreException e) {
            log.error("error getting entity", e);
            throw new NotFoundException(e);
        }
    }


    @PUT
    @Path("/type/{id}/entity/{entityId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EntityVO updateEntity(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId,
            ChangeSummaryVO vo) {
        return persistentStoreService.updateEntity(id, entityId, vo);
    }


    @POST
    @Path("/type/{id}/entity")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EntityVO newEntity(
            @PathParam("id") int id,
            ChangeSummaryVO vo) {
        return persistentStoreService.newEntity(id, vo);
    }

    @DELETE
    @Path("/type/{id}/entity/{entityId}")
    public void deleteEntity(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId) {
        persistentStoreService.deleteEntity(id, entityId);
    }

    @GET
    @Path("/type/{id}/entity/{entityId}/blob/{blobName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getBlob(
            @PathParam("id") int id,
            @PathParam("entityId") long entityId,
            @PathParam("blobName") String blobName) {
        return outputStream -> persistentStoreService.getBlob(id, entityId, blobName, outputStream);
    }

}
