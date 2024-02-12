float unpackVec3ToFloat(vec3 packedValue, float minValue, float maxValue)
{
    float packScale = maxValue - minValue;
    float result = dot(packedValue, 1.0 / vec3(1.0, 256.0, 256.0 * 256.0));
    return minValue + packScale * result * (256.0 * 256.0 * 256.0) / (256.0 * 256.0 * 256.0 - 1.0);
}
