package world.worldBuilder;

public class WorldBuilder {
    private final int OCTAVES = 8;
    private final float ROUGHNESS = 0.5f, SCALE = 0.5f;
    private final NoiseGenerator CONTINENTALITY_NOISE, WEIRDNESS_NOISE, RIVERS_NOSISE;

    public WorldBuilder(int seed) {
        this.CONTINENTALITY_NOISE = new NoiseGenerator(seed) {{SetNoiseType(NoiseType.OpenSimplex2);}};
        this.WEIRDNESS_NOISE = new NoiseGenerator(seed+1) {{SetNoiseType(NoiseType.OpenSimplex2);}};
        this.RIVERS_NOSISE = new NoiseGenerator(seed+2) {{
            SetNoiseType(NoiseType.OpenSimplex2);
        }};
    }

    public double getContinentalityAt(int x, int y) {
        return this.getNoiseAt(x, y, this.CONTINENTALITY_NOISE, this.SCALE, this.OCTAVES, this.ROUGHNESS);
    }

    public double getWeirdnessAt(int x, int y) {
        return this.getNoiseAt(x, y, this.WEIRDNESS_NOISE, this.SCALE, this.OCTAVES, this.ROUGHNESS);
    }

    public double getRiversAt(int x, int y) {
        return this.getNoiseAt(x, y, this.RIVERS_NOSISE, this.SCALE /3, this.OCTAVES, this.ROUGHNESS * 1.5);
    }

    public double getNoiseAt(int x, int y, NoiseGenerator noiseGenerator, double scale, double octaves, double roughness) {
        double noise = 0;
        double layerFrequency = this.SCALE;
        double layerWeight = 1;

        for (int octave = 0; octave < this.OCTAVES; octave++) {
            noise += noiseGenerator.GetNoise((float) (x * layerFrequency), (float) (y * layerFrequency)) * layerWeight;
            layerFrequency *= 2;
            layerWeight *= this.ROUGHNESS;
        }
        return noise;
    }
}
