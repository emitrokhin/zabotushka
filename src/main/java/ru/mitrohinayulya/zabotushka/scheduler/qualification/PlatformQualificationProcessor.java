package ru.mitrohinayulya.zabotushka.scheduler.qualification;

import ru.mitrohinayulya.zabotushka.entity.Platform;

public interface PlatformQualificationProcessor {

    Platform platform();

    QualificationProcessStats processQualifications();
}
