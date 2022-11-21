package nextstep.subway.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Embeddable
public class Sections {

    private static final int MIN_REMOVEABLE_SECTION_SIZE = 2;

    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    protected Sections() {}

    private Sections(List<Section> sections) {
        this.sections = new ArrayList<>(sections);
    }

    public static Sections from(List<Section> sections) {
        return new Sections(sections);
    }

    public int size() {
        return this.sections.size();
    }

    public void add(Section section) {
        validateAddContainsAllStation(section);
        validateAddNonContainsStation(section);
        updateSections(section);
        sections.add(section);
    }

    private void validateAddContainsAllStation(Section section) {
        List<Station> stations = getStations();
        if (!stations.isEmpty() && stations.containsAll(section.getStations())) {
            throw new IllegalArgumentException("이미 모든 역이 존재 합니다.");
        }
    }

    private void validateAddNonContainsStation(Section section) {
        List<Station> stations = getStations();
        if (!stations.isEmpty() && stations.stream().noneMatch(section.getStations()::contains)) {
            throw new IllegalArgumentException("등록하려는 구간의 상행역과 하행역 둘 중 하나라도 기존 구간의 역에 포함되지 않으면 구간을 등록할 수 없습니다.");
        }
    }

    private void updateSections(Section source) {
        sections.stream().forEach(section -> section.update(source));
    }

    public void remove(Optional<Section> upSection, Optional<Section> downSection) {
        validateRemoveExistSections(upSection, downSection);
        validateRemoveLastSection();
        removeFirstSection(upSection, downSection);
        removeLastSection(upSection, downSection);
        removeMiddleSection(upSection, downSection);
    }

    private void validateRemoveExistSections(Optional<Section> upSection, Optional<Section> downSection) {
        if (!upSection.isPresent() && !downSection.isPresent()) {
            throw new IllegalArgumentException("구간이 존재하지 않습니다.");
        }
    }

    private void validateRemoveLastSection() {
        if (sections.size() < MIN_REMOVEABLE_SECTION_SIZE) {
            throw new IllegalArgumentException("더이상 구간을 삭제할 수 없습니다.");
        }
    }

    private void removeFirstSection(Optional<Section> upSection, Optional<Section> downSection) {
        if (!upSection.isPresent() && downSection.isPresent()) {
            sections.remove(downSection.get());
        }
    }

    private void removeLastSection(Optional<Section> upSection, Optional<Section> downSection) {
        if (upSection.isPresent() && !downSection.isPresent()) {
            sections.remove(upSection.get());
        }
    }

    private void removeMiddleSection(Optional<Section> upSection, Optional<Section> downSection) {
        if (upSection.isPresent() && downSection.isPresent()) {
            upSection.get().extend(downSection.get());
            sections.remove(downSection.get());
        }
    }

    public void order() {
        Optional<Section> firstSection = findFirstSection();
        if (!firstSection.isPresent()) {
            return;
        }
        moveLast(firstSection.get());

        Section currentLastSection = firstSection.get();
        while (true) {
            Optional<Section> nextSection = findNextSection(currentLastSection.getDownStation());
            if (!nextSection.isPresent()) {
                break;
            }
            moveLast(nextSection.get());
            currentLastSection = nextSection.get();
        }
    }

    private Optional<Section> findFirstSection() {
        return sections.stream()
                .filter(section -> !matchByDownStation(section.getUpStation()))
                .findAny();
    }

    private boolean matchByDownStation(Station station) {
        return sections.stream()
                .map(section -> section.getDownStation())
                .anyMatch(downStation -> station.equalsId(downStation));
    }

    private Optional<Section> findNextSection(Station downStation) {
        return sections.stream()
                .filter(section -> downStation.equalsId(section.getUpStation()))
                .findAny();
    }

    private void moveLast(Section section) {
        sections.remove(section);
        sections.add(section);
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public List<Station> getStations() {
        return sections.stream()
                .map(Section::getStations)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }
}
