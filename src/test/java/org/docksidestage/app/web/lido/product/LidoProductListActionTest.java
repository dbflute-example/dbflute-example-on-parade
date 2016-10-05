package org.docksidestage.app.web.lido.product;

import org.dbflute.optional.OptionalThing;
import org.dbflute.utflute.lastaflute.mock.TestingJsonData;
import org.docksidestage.app.web.base.paging.SearchPagingResult;
import org.docksidestage.unit.UnitOnparadeTestCase;
import org.lastaflute.web.response.JsonResponse;

/**
 * @author jflute
 */
public class LidoProductListActionTest extends UnitOnparadeTestCase {

    public void test_index_searchByName() {
        // ## Arrange ##
        LidoProductListAction action = new LidoProductListAction();
        inject(action);
        ProductSearchBody body = new ProductSearchBody();
        body.productName = "P";

        // ## Act ##
        JsonResponse<SearchPagingResult<ProductRowResult>> response = action.index(OptionalThing.of(1), body);

        // ## Assert ##
        showJson(response);
        TestingJsonData<SearchPagingResult<ProductRowResult>> data = validateJsonData(response);
        data.getJsonBean().rows.forEach(bean -> {
            log(bean);
            assertTrue(bean.productName.contains(body.productName));
        });
    }
}
