package com.superchat.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
public class ClientProfile {
    private String name;
    private Integer age;
    private String maritalStatus;
    private Boolean hasChildren;
    private Boolean hasPets;
    private Boolean hasHouses;
    private Boolean hasApartments;
    private Boolean hasCars;
    private Boolean likesTraveling;
    private StringBuilder expressionOfInterestInInsurance = new StringBuilder();
    private StringBuilder expressionOfInterestInOthersThings = new StringBuilder();

    private String error;
    private String message;
    private String raw;

    public void applyJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            String nameFromJson = getText(root, "name");
            if (nameFromJson != null && !nameFromJson.isEmpty())
                this.setName(nameFromJson);

            String maritalStatusFromJson = getText(root, "maritalStatus");
            if (maritalStatusFromJson != null && !maritalStatusFromJson.isEmpty())
                this.setMaritalStatus(maritalStatusFromJson);

            Integer ageFromJson = getInt(root, "age");
            if (ageFromJson != null)
                this.setAge(ageFromJson);

            Boolean hasChildrenFromJson = getBoolean(root, "hasChildren");
            if (hasChildrenFromJson != null)
                this.setHasChildren(hasChildrenFromJson);

            Boolean hasPetsFromJson = getBoolean(root, "hasPets");
            if (hasPetsFromJson != null)
                this.setHasPets(hasPetsFromJson);

            Boolean hasHousesFromJson = getBoolean(root, "hasHouses");
            if (hasHousesFromJson != null)
                this.setHasHouses(hasHousesFromJson);

            Boolean hasApartmentsFromJson = getBoolean(root, "hasApartments");
            if (hasApartmentsFromJson != null)
                this.setHasApartments(hasApartmentsFromJson);

            Boolean hasCarsFromJson = getBoolean(root, "hasCars");
            if (hasCarsFromJson != null)
                this.setHasCars(hasCarsFromJson);

            Boolean likesTravelFromJson = getBoolean(root, "likesTraveling");
            if (likesTravelFromJson != null)
                this.setLikesTraveling(likesTravelFromJson);

            //expressionOfInterestInInsurance.setLength(0); // Clear previous content
            String expressionOfInterestInInsuranceFromJson = getText(root, "expressionOfInterestInInsurance");
            if (expressionOfInterestInInsuranceFromJson != null && !expressionOfInterestInInsuranceFromJson.isEmpty()) {
                if (!expressionOfInterestInInsurance.isEmpty())
                    expressionOfInterestInInsurance.append(", ");
                expressionOfInterestInInsurance.append(expressionOfInterestInInsuranceFromJson);
            }


            if (hasPetsFromJson != null && hasPetsFromJson && !expressionOfInterestInInsurance.toString().toLowerCase().contains("pet"))
                expressionOfInterestInInsurance.append("Pet insurance, ");

            if (hasHousesFromJson != null && hasHousesFromJson && !expressionOfInterestInInsurance.toString().toLowerCase().contains("home"))
                expressionOfInterestInInsurance.append("Home (house) insurance, ");

            if (hasApartmentsFromJson != null && hasApartmentsFromJson && !expressionOfInterestInInsurance.toString().toLowerCase().contains("apartment"))
                expressionOfInterestInInsurance.append("Home (apartment) insurance, ");

            if (hasCarsFromJson != null && hasCarsFromJson && !expressionOfInterestInInsurance.toString().toLowerCase().contains("car"))
                expressionOfInterestInInsurance.append("Car insurance, ");

            if (likesTravelFromJson != null && likesTravelFromJson && !expressionOfInterestInInsurance.toString().toLowerCase().contains("travel"))
                expressionOfInterestInInsurance.append("Travel insurance, ");

            String expressionOfInterestInOthersThingsFromJson = getText(root, "expressionOfInterestInOthersThings");
            if (expressionOfInterestInOthersThingsFromJson != null && !expressionOfInterestInOthersThingsFromJson.isEmpty())
                expressionOfInterestInInsurance.append(expressionOfInterestInOthersThingsFromJson).append(", ");

            String errorFromJson = getText(root, "error");
            if (errorFromJson != null && !errorFromJson.isEmpty())
                this.setError(errorFromJson);

            String messageFromJson = getText(root, "message");
            if (messageFromJson != null && !messageFromJson.isEmpty())
                this.setMessage(messageFromJson);

            String rawFromJson = getText(root, "raw");
            if (rawFromJson != null && !rawFromJson.isEmpty())
                this.setRaw(rawFromJson);

        } catch (Exception e) {
            log.error("Error building ClientProfile from JSON", e);
        }
    }

    private static String getText(JsonNode root, String field) {
        if (root.has(field) && !root.get(field).isNull()) {
            return root.get(field).asText();
        }
        return null;
    }

    private static Integer getInt(JsonNode root, String field) {
        if (root.has(field) && !root.get(field).isNull()) {
            return root.get(field).asInt();
        }
        return null;
    }

    private static Boolean getBoolean(JsonNode root, String field) {
        if (root.has(field) && !root.get(field).isNull()) {
            return root.get(field).asBoolean();
        }
        return null;
    }

    private String buildAgeBand() {
        if (age == null)
            return "Unknown";

        if (age <= 12)
            return "Childhood";
        else if (age <= 20)
            return "Adolescence";
        else if (age <= 26)
            return "Youth";
        else if (age <= 59)
            return "Adulthood";
        else if (age <= 65)
            return "Senior";
        else
            return "Elderly";
    }

    public String friendlyProfileDescription() {
        String ageBand = this.buildAgeBand();
        String marital = (this.getMaritalStatus() == null || this.getMaritalStatus().isBlank())
                ? "Unknown marital status"
                : this.getMaritalStatus();
        String children  = (this.getHasChildren() == null)
                ? "Unknown if has children"
                : (this.getHasChildren() ? "With children" : "With No children");

        String pets = (this.getHasPets() == null)
                ? "Unknown if has pets"
                : (this.getHasPets() ? "With pets" : "With No pets");

        String houses = (this.getHasHouses() == null)
                ? "Unknown if has houses"
                : (this.getHasHouses() ? "With houses" : "With No houses");

        String apartments = (this.getHasApartments() == null)
                ? "Unknown if has apartments"
                : (this.getHasApartments() ? "With apartments" : "With No apartments");

        String cars = (this.getHasCars() == null)
                ? "Unknown if has cars"
                : (this.getHasCars() ? "With cars" : "With No cars");

        String insuredInterest = (expressionOfInterestInInsurance == null || expressionOfInterestInInsurance.isEmpty())
                ? "interests: unknown"
                : "interested in " + expressionOfInterestInInsurance.toString();

        String othersInterest = (expressionOfInterestInOthersThings == null || expressionOfInterestInOthersThings.isEmpty())
                ? "other interests: unknown"
                : "others interests are " + expressionOfInterestInOthersThings.toString();

        return """
               Find target audience that matches: age %s (%s), %s, %s, %s, %s, %s, %s. %s, %s.
               Return insurance products whose TARGET AUDIENCE best fits this profile.
               """
                .formatted(
                (age == null || age == 0 ? "unknown" : String.valueOf(age)),
                ageBand,
                marital,
                children,
                hasPets != null ? pets: "",
                hasHouses != null ? houses: "",
                hasApartments != null ? apartments : "",
                hasCars != null ? cars : "",
                expressionOfInterestInInsurance == null || expressionOfInterestInInsurance.isEmpty() ? "" : insuredInterest,
                expressionOfInterestInOthersThings == null || expressionOfInterestInOthersThings.isEmpty() ?  "" : othersInterest
        );
    }

    public Boolean isEnoughDataForRecommendProducts() {
        return (this.age != null || this.maritalStatus != null ||
                this.hasChildren != null || this.hasPets != null || this.hasHouses != null ||
                this.hasApartments != null || this.hasCars != null ||
                (this.expressionOfInterestInInsurance != null && !this.expressionOfInterestInInsurance.isEmpty()) ||
                (this.expressionOfInterestInOthersThings != null && !this.expressionOfInterestInOthersThings.isEmpty()));
    }

    @Override
    public String toString() {
        return "ClientProfile {" +
                "name='" + name + "'" +
                ", age=" + age +
                ", maritalStatus='" + maritalStatus + "'" +
                ", hasChildren=" + hasChildren +
                ", hasPets=" + hasPets +
                ", hasHouses=" + hasHouses +
                ", hasApartment=" + hasApartments +
                ", hasCars=" + hasCars +
                ", expressionOfInterestInInsurance='" + expressionOfInterestInInsurance + "'" +
                ", expressionOfInterestInOthersThings='" + expressionOfInterestInOthersThings + "'" +
                ", error='" + error + "'" +
                ", message='" + message + "'" +
                ", raw='" + raw + "'" +
                '}';
    }
}