package ru.mitrohinayulya.zabotushka.qualification.domain;

import ru.mitrohinayulya.zabotushka.shared.domain.QualificationLevel;

/// Port: what the rest of the system needs from the qualification subsystem.
///
/// The Access and Membership bounded contexts depend on this port, NOT on the
/// Greenway infrastructure classes. This is the key anti-corruption layer boundary.
///
/// The implementation ({@code GreenwayQualificationAdapter} in
/// {@code qualification.infrastructure}) calls the Greenway API and translates the
/// result into the shared-kernel {@link QualificationLevel}.
///
/// <h2>Why this matters</h2>
/// Currently {@code AbstractGroupAccessService} and {@code AbstractJoinRequestService}
/// inject {@code GreenwayQualificationService} directly — a concrete infrastructure class.
/// If Greenway's API changes, or you add a second qualification data source, you must
/// change the abstract services. With this port, the domain never changes for
/// infrastructure reasons.
public interface QualificationPort {

    QualificationLevel getBestQualification(long greenwayId);
}
