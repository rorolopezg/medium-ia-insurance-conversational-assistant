package com.superchat.services;

import com.superchat.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public final class ProductService {

    private ProductService(){}

    public List<Product> findAllProducts() {
        List<Product> items = new ArrayList<>();

        items.add(new Product(
            "PROD_01",
            "Individual Life Insurance",
            "Insurance designed to provide financial protection to your loved ones in case of death.",
            """
            - Natural death: Provides a benefit for death due to natural causes.
            - Accidental death: Covers death by accidents, offering an additional benefit.
            """,
            """
            Adults aged 25-65, of any gender, who are primary income earners or have financial dependents
            (such as spouses, children, or elderly parents), seeking to ensure the financial security and
            well-being of their families in the event of unforeseen circumstances.
            """,
            25, 65, "life"
        ));

        items.add(new Product(
            "PROD_02",
            "Personal Accident Insurance",
            "Insurance that offers protection in case of accidents resulting in injuries or death.",
            """
            - Accidental death: Provides a benefit for death due to accidents.
            - Permanent disability: Covers permanent disability resulting from an accident, offering financial
              benefits.
            """,
            """
            Adults aged 25-65, of any gender, who are exposed to risks of accidents in their daily activities,
            such as workers, students, athletes, or people who frequently travel, and who wish to protect themselves
            and their families from the financial consequences of accidental injuries or death.
            """,
            25, 65, "accident"
        ));

        items.add(new Product(
            "PROD_03",
            "Health Insurance",
            "Insurance that covers medical expenses for illnesses or accidents.",
            """
            - Hospitalization: Covers costs of hospitalization due to illness or accident.
            - Surgical procedures: Covers expenses for surgeries required due to health issues.
            - Medical consultations: Provides coverage for medical consultations with specialists.
            """,
            """
            Individuals and families of all ages (18-120) who are concerned about potential medical expenses due to
            illness or accidents, including those with pre-existing health conditions, self-employed professionals,
            parents seeking coverage for their children, elderly individuals, and anyone who wants to ensure access
            to quality healthcare and financial protection against unexpected medical costs.
            """,
            18, 120, "health"
        ));

        items.add(new Product(
            "PROD_04",
            "Young Adult Travel Insurance",
            """
            A comprehensive travel insurance plan designed for young adults who seek adventure, exploration, and
            peace of mind while traveling. It offers essential protection against unexpected events that may
            occur during domestic or international trips, allowing you to focus on enjoying your journey without
            worries.
            """,
            """
            - Medical emergencies abroad: Covers medical expenses resulting from illness or accidents during your trip.
            - Trip cancellation or interruption: Provides reimbursement for non-refundable expenses if your trip is
              canceled or cut short due to covered reasons.
            - Lost or delayed baggage: Compensates for lost, stolen, or significantly delayed luggage.
            - Travel assistance services: Offers 24/7 support for emergencies, including medical evacuation, legal
              assistance, and travel advice.
            """,
            """
            Young single adults aged 18–35, of any gender, who travel for leisure, study, or work and seek reliable
            protection against travel-related risks. Ideal for frequent travelers, digital nomads, students studying
            abroad, or professionals on business trips who value safety, flexibility, and peace of mind while
            exploring the world..
            """,
            18, 35, "travel"
        ));

        items.add(new Product(
            "PROD_05",
            "Pets Insurance",
            "Insurance that covers medical expenses for illnesses or accidents of your loved pet.",
            """
            - Hospitalization: Covers costs of hospitalization due to illness or accident.
            - Surgical procedures: Covers expenses for surgeries required due to health issues.
            - Medical consultations: Provides coverage for medical consultations with specialists.
            """,
            """
            Oriented to people of all ages (18-120), owners of pets such as dogs and cats, who want to provide them
            with protection against diseases.
            """,
            18, 120, "pet"
        ));

        items.add(new Product(
            "PROD_06",
            "Home Insurance",
            "Insurance that protects your home’s structure and contents against covered events and includes personal liability coverage.",
            """
            - Fire and smoke: Covers damage to the dwelling and contents caused by fire or smoke.
            - Theft and vandalism: Covers stolen belongings and damage from forced entry or malicious acts.
            - Water damage (sudden/accidental): Covers damage from burst pipes or appliance leaks (non-gradual).
            - Natural events: Windstorm and hail; earthquake/flood available via optional endorsements.
            - Glass breakage and fixtures: Covers windows, sanitary ware, and fixed installations.
            - Temporary accommodation (loss of use): Pays for lodging if the home becomes uninhabitable due to a covered loss.
            - Personal liability: Covers injuries to third parties or damage to their property caused by the insured household.
            """,
            """
            Adults who own or rent a house or apartment and want financial protection for their dwelling, belongings, and liability.
            Ideal for first-time homeowners, families, and landlords seeking comprehensive home coverage.
            """,
            21, 75, "home"
        ));

        items.add(new Product(
            "PROD_07",
            "Car Insurance",
            "Insurance that protects your car and your liability arising from its use, covering damage, theft, third-party claims, and roadside emergencies.",
            """
            - Third-party liability (bodily injury/property damage): Covers injuries to others and damage to their property caused by your car.
            - Collision: Pays for repairs to your car after a crash, regardless of fault (subject to deductible).
            - Comprehensive: Covers non-collision losses (theft, fire, vandalism, falling objects, weather events).
            - Medical payments / personal injury protection: Covers medical expenses for you and your passengers after an accident.
            - Uninsured/underinsured motorist: Protects you if the at-fault driver has insufficient or no insurance.
            - Roadside assistance & towing: Help for breakdowns, flat tires, dead batteries, and emergency towing.
            - Glass coverage: Repairs or replaces damaged windshields and windows.
            - Rental car / mobility allowance: Provides a temporary vehicle while yours is being repaired after a covered loss.
            - Optional accessories & custom parts: Extends coverage to added equipment (sound systems, racks, custom wheels).
            """,
            """
            Licensed drivers who own or lease a car and want financial protection for their vehicle and liability.
            Ideal for commuters, families, and everyday drivers; optional endorsements available for ride-share or delivery use.
            """,
            18, 75, "auto"
        ));

        return items;
    }
}
