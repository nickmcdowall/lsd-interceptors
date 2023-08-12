package io.lsdconsulting.intercceptors.example.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Fish {

    @Id
    private Long id;

    @Column
    private String name;

    public Fish(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Fish() {
    }

    public static FishBuilder builder() {
        return new FishBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Fish)) return false;
        final Fish other = (Fish) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Fish;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        return result;
    }

    public String toString() {
        return "Fish(id=" + this.getId() + ", name=" + this.getName() + ")";
    }

    public FishBuilder toBuilder() {
        return new FishBuilder().id(this.id).name(this.name);
    }

    public static class FishBuilder {
        private Long id;
        private String name;

        FishBuilder() {
        }

        public FishBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public FishBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Fish build() {
            return new Fish(this.id, this.name);
        }

        public String toString() {
            return "Fish.FishBuilder(id=" + this.id + ", name=" + this.name + ")";
        }
    }
}
