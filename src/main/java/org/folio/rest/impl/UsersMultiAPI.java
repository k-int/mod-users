/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.User;
import org.folio.rest.jaxrs.model.UserdataMultipleSummary;
import org.folio.rest.jaxrs.resource.UsersMultiResource;
import org.folio.rest.persist.MongoCRUD;
import org.folio.rest.tools.messages.MessageConsts;
import org.folio.rest.tools.messages.Messages;

/**
 *
 * @author kurt
 */
public class UsersMultiAPI implements UsersMultiResource {
  
  private final Messages messages = Messages.getInstance();
  private final String USER_COLLECTION = "user";
  private static final String USER_ID_FIELD = "id";
  private static final String USER_NAME_FIELD = "username";
  private final Logger logger = LoggerFactory.getLogger(UsersAPI.class);
  
  @Validate
  @Override
  public void postUsersMulti(String lang, List<User> entity,
          Map<String, String> okapiHeaders, 
          Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
          throws Exception {
    try {
      vertxContext.runOnContext( v -> {
        List<String> idList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();
        List<Future> futureList = new ArrayList<>();
        for(User user : entity) {
          Future<UserdataMultipleSummary> future = Future.future();
          UserdataMultipleSummary ums = new UserdataMultipleSummary();
          ums.setId(user.getId());
          ums.setUsername(user.getUsername());
          if(idList.contains(user.getId()) || nameList.contains(user.getUsername())) {
            ums.setSuccessful(false);
            ums.setMessage("Duplicate username or id");
            future.complete(ums);
          } else {
            nameList.add(user.getUsername());
            idList.add(user.getId());
            try {
              MongoCRUD.getInstance(vertxContext.owner()).save(USER_COLLECTION,
                        entity, reply -> {
             ums.setSuccessful(true);
             future.complete(ums);
             });
            } catch(Exception e) {
                ums.setSuccessful(false);
                ums.setMessage(e.getMessage());
            }
          }
          futureList.add(future);
        }
        CompositeFuture compFut = CompositeFuture.all(futureList);
        compFut.setHandler(res -> {
          if(res.succeeded()) {
            asyncResultHandler.handle(Future.succeededFuture(
                    PostUsersMultiResponse.withJsonCreated(compFut.list())));
          } else {
            asyncResultHandler.handle(Future.succeededFuture(
                    PostUsersMultiResponse.withPlainInternalServerError(
                            messages.getMessage(
                                    lang, MessageConsts.InternalServerError))));
          }
        });        
      });
    } catch(Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(
              PostUsersMultiResponse.withPlainInternalServerError(
                      messages.getMessage(lang, MessageConsts.InternalServerError))));      
    }
  }

  @Override
  public void getUsersMulti(String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
    asyncResultHandler.handle(Future.succeededFuture(
             PostUsersMultiResponse.withPlainInternalServerError(
                     messages.getMessage(lang, MessageConsts.InternalServerError))));
  }
  
}
