const mongoose = require("mongoose");

const Schema = mongoose.Schema;

let SensorSchema = new Schema(
    {
        category: {
            type: String,
            enum: [
                'temperature',
                'clock',
                'sonarVirtual',
                'sonarRobot'
            ]
        },
        name: { type: String },
        value: { type: Schema.Types.Mixed },
        unit: { type: String },
        code: { type: Number, unique: true, required: true },
        minValue: { type: Number },
        maxValue: { type: Number }
    }
);

module.exports = mongoose.model("Sensor", SensorSchema);