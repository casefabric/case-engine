/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.casefabric.ai.agent.sales;

import com.casefabric.ai.tools.BraveSearchToolGroup;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.core.CoreToolGroups;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Agent(description = "Researches a sales lead and qualifies how likely it is to close a deal", version = "0.0.1")
@Profile("!test")
public class SalesLeadQualifier {

    @AchievesGoal(
            description = "The sales lead is qualified and key decision makers are matched with the request",
            export = @Export(remote = true, name = "QualifyLead", startingInputTypes = { UserInput.class }))
    @Action(toolGroups = { CoreToolGroups.WEB, CoreToolGroups.MATH, CoreToolGroups.BROWSER_AUTOMATION})
    QualifiedLead qualifyLead(UserInput userInput, Company company, OperationContext context) {
        return context
                .ai()
                .withAutoLlm()
                .withPromptContributor(Personas.QUALIFIER)
                .createObject(String.format("""
                                You will be given a lead to target for a sell and a number of employees of the prospect
                                Analyse the lead to match our product offering as found on https://casefabric.com
                                and https://guide.casefabric.com
                                fetch the information of these websites and compare them to the information fetched
                                from the company and given in this prompt.
                                
                                #Company URL
                                %s
                                
                                #Company products
                                %s
                                
                                Qualify if the company needs require the casefabric offerings in a range from 1 to 10
                                and store the outcome in the qualification field as an integer from 1 to 10
                                
                                # User input containing the lead information.
                                %s
                                """,
                        company.website(),
                        company.products(),
                        userInput.getContent()
                ), QualifiedLead.class);
    }

    @AchievesGoal(
            description = "Find company details",
            export = @Export(remote = true, name = "FindCompany", startingInputTypes = { UserInput.class }))
    @Action()
    Company findCompany(UserInput userInput, OperationContext context) {
        return context.ai()
                .withLlm(LlmOptions.withAutoLlm().withTemperature(0.01))
                .withPromptContributor(Personas.RESEARCHER)
                .withToolGroups(Set.of(BraveSearchToolGroup.BRAVE_SEARCH, CoreToolGroups.WEB, CoreToolGroups.BROWSER_AUTOMATION))
                .createObject(String.format("""
                        Find the exact company URL via brave search that is connected to the email address
                        given in the user input.
                        Put the company URL in the website field
                        Use the company website url to find the name of the company and put it in the name field.
                        Use the company website url to find the companies product offerings and summarize that into the products field.
                        
                        Ensure that the company profile contains email domains that match the domain name of the user input.
                        NEVER use a tool towards https://linkedin.com in any way

                        # User input containing the lead information.
                        %s
                        """,
                        userInput.getContent()), Company.class);
    }

}