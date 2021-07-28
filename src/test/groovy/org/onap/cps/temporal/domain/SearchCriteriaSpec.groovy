package org.onap.cps.temporal.domain

import org.springframework.data.domain.Sort
import spock.lang.Specification

import java.time.OffsetDateTime

class SearchCriteriaSpec extends Specification {

    def myDataspace = "my-dataspace"
    def myAnchorNames = ["my-anchort"] as Set
    def myschemaSetName = "my-schemaset"


    def 'Search Criteria created with only mandatory field has default values for others.'() {

        when: 'search criteria is built'
            def searchCriteria = SearchCriteria.builder()
                    .dataspaceName(myDataspace)
                    .schemaSetName(myschemaSetName)
                    .pagination(0, 10)
                    .build()

        then: 'search criteria has default value for sort'
            searchCriteria.getPageable().getSort() == Sort.by(Sort.Direction.DESC, "observed_timestamp")
        and: 'pointInTime has almost current time as default value'
            OffsetDateTime.now().minusMinutes(5).isBefore(searchCriteria.getPointInTime())
        and: 'contains the provided value to builder'
            searchCriteria.getDataspaceName() == myDataspace
            searchCriteria.getSchemaSetName() == myschemaSetName
            searchCriteria.getPageable().getPageNumber() == 0
            searchCriteria.getPageable().getPageSize() == 10

    }

    def 'Error handling : missing dataspace.'() {
        when: "search criteria is created"
            SearchCriteria.builder()
                .anchorNames(myAnchorNames as Set)
                .pagination(0, 10)
                .build()
        then: 'exception is thrown'
            thrown(IllegalStateException)
    }

    def 'Error handling : missing both schemaset and anchors.'() {
        when: "search criteria is created"
        SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .pagination(0, 10)
                .build()
        then: 'exception is thrown'
        thrown(IllegalStateException)
    }

    def 'Error handling : missing pagination.'() {
        when: "search criteria is created"
        SearchCriteria.builder()
                .dataspaceName(myDataspace)
                .anchorNames(myAnchorNames)
                .build()
        then: 'exception is thrown'
        thrown(IllegalStateException)
    }

}
